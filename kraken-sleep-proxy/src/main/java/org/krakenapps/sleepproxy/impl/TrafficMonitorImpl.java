package org.krakenapps.sleepproxy.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.pcap.decoder.arp.ArpPacket;
import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.EthernetHeader;
import org.krakenapps.pcap.decoder.ethernet.EthernetType;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.decoder.tcp.TcpSegment;
import org.krakenapps.pcap.decoder.tcp.TcpSegmentCallback;
import org.krakenapps.pcap.decoder.tcp.TcpSession;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.util.Arping;
import org.krakenapps.pcap.util.IpConverter;
import org.krakenapps.pcap.util.PcapLiveRunner;
import org.krakenapps.pcap.util.WakeOnLan;
import org.krakenapps.sleepproxy.LogListener;
import org.krakenapps.sleepproxy.LogProvider;
import org.krakenapps.sleepproxy.TrafficMonitor;
import org.krakenapps.sleepproxy.model.Agent;
import org.krakenapps.sleepproxy.model.NetworkAdapter;
import org.krakenapps.sleepproxy.model.SleepLog.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sleep-proxy-traffic-monitor")
@Provides(specifications = { TrafficMonitor.class })
public class TrafficMonitorImpl implements TrafficMonitor, Runnable, LogListener {
	private Logger logger = LoggerFactory.getLogger(TrafficMonitorImpl.class.getName());

	@Requires
	private LogProvider provider;

	// storages
	private Map<InetAddress, MacAddress> backups;
	private Map<InetAddress, Set<InetAddress>> targets;
	private Map<InetAddress, MacAddress> arpCache;

	// threads
	private Thread attackThread;
	private Thread arpThread;
	private Thread synThread;

	// monitors
	private ArpUpdater arpUpdater;
	private SynMonitor synMonitor;

	private volatile boolean stop = false;
	private Object sync;

	public TrafficMonitorImpl() {
		arpCache = new ConcurrentHashMap<InetAddress, MacAddress>();
		backups = new HashMap<InetAddress, MacAddress>();
		targets = new HashMap<InetAddress, Set<InetAddress>>();
		sync = new Object();
	}

	@Validate
	public void start() {
		try {
			provider.register(this);

			logger.info("sleep proxy: loading arp cache");
			arpUpdater = new ArpUpdater();
			// ensure arp cache data
			arpUpdater.runOnce();
			logger.info("sleep proxy: arp cache loaded");

			arpThread = new Thread(arpUpdater, "Sleep Proxy Arp Cache Updater");
			arpThread.start();

			attackThread = new Thread(this, "Sleep Proxy Arp Poisoner");
			attackThread.start();

			try {
				PcapDevice device = PcapDeviceManager.openFor(InetAddress.getByName("8.8.8.8"), 1000);
				synMonitor = new SynMonitor(device);
				synThread = new Thread(synMonitor, "Sleep Proxy Syn Monitor");
				synThread.start();
			} catch (Exception e) {
				logger.error("sleep proxy: cannot open syn monitor", e);
			}
		} catch (Exception e) {
			logger.error("sleep proxy: cannot start traffic monitor", e);
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Invalidate
	public void stop() {
		stop = true;

		if (provider != null)
			provider.unregister(this);

		try {
			if (attackThread != null)
				attackThread.interrupt();

			if (arpUpdater != null)
				arpUpdater.close();

			if (synMonitor != null)
				synMonitor.close();

			if (synThread != null)
				synThread.interrupt();
		} catch (Exception e) {
			logger.error("sleep proxy: error while stopping", e);
		}
	}

	@Override
	public void run() {
		stop = false;
		while (!stop) {
			try {
				runOnce();
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}
	}

	private void runOnce() {
		PcapDevice device = null;
		try {
			InetAddress google = InetAddress.getByName("8.8.8.8");
			device = PcapDeviceManager.openFor(google, 1000);

			for (InetAddress ip : targets.keySet()) {
				Set<InetAddress> s = targets.get(ip);
				for (InetAddress target : s)
					send(device, arpCache, device.getMetadata().getMacAddress(), ip, target);
			}

		} catch (UnknownHostException e) {
		} catch (IOException e) {
			logger.error("sleep proxy: traffic monitor run failed", e);
		} finally {
			if (device != null)
				try {
					device.close();
				} catch (IOException e) {
				}
		}
	}

	@Override
	public Map<InetAddress, MacAddress> getArpCache() {
		return new HashMap<InetAddress, MacAddress>(arpCache);
	}

	@Override
	public void onReceive(Agent agent, Status status) {
		if (status == Status.Suspend) {
			for (NetworkAdapter adapter : agent.getAdapters()) {
				try {
					register(InetAddress.getByName(adapter.getIp()));
				} catch (UnknownHostException e) {
					logger.error("sleep proxy: invalid ip", e);
				}
			}
		} else if (status == Status.Resume) {
			for (NetworkAdapter adapter : agent.getAdapters()) {
				try {
					unregister(InetAddress.getByName(adapter.getIp()));
				} catch (UnknownHostException e) {
					logger.error("sleep proxy: invalid ip", e);
				}
			}
		}
	}

	@Override
	public void register(InetAddress ip) {
		if (arpCache == null) {
			logger.error("sleep proxy: arp cache is null, cannot monitor {}", ip.getHostAddress());
			throw new IllegalStateException("arp cache is null");
		}

		MacAddress mac = null;
		try {
			mac = Arping.query(ip, 2000);
			if (mac == null) {
				logger.error("sleep proxy: mac not found for ip {}", ip.getHostAddress());
				throw new IllegalStateException("cannot find mac for ip " + ip.getHostAddress());
			}

			if (targets.containsKey(ip)) {
				logger.error("sleep proxy: duplicated target ip {}", ip.getHostAddress());
				return;
			}
		} catch (IOException e) {
			logger.error("sleep proxy: cannot monitor ip {}", ip.getHostAddress());
			return;
		}

		Set<InetAddress> s = new HashSet<InetAddress>();
		synchronized (sync) {
			logger.info("sleep proxy: backup ip {}, mac {}", ip, mac);
			backups.put(ip, mac);

			for (InetAddress target : arpCache.keySet()) {
				targets.put(ip, s);
				if (!target.equals(ip))
					s.add(target);
			}
		}

		// attack
		PcapDevice device = null;
		try {
			device = PcapDeviceManager.openFor(ip, 10000);

			for (InetAddress target : s) {
				send(device, arpCache, device.getMetadata().getMacAddress(), ip, target);
			}
		} catch (IOException e) {
			logger.error("sleep proxy: attack failed for " + ip.getHostAddress(), e);
		} finally {
			if (device != null)
				try {
					device.close();
				} catch (IOException e) {
				}
		}
	}

	@Override
	public void unregister(InetAddress ip) {
		if (arpCache == null) {
			logger.error("sleep proxy: arp cache is null, cannot unregister ip {}", ip.getHostAddress());
			throw new IllegalStateException("arp cache is null");
		}

		logger.info("sleep proxy: try to recover poison for {}", ip);

		PcapDeviceMetadata metadata = PcapDeviceManager.getDeviceMetadata(ip);
		PcapDevice device = null;
		try {
			device = PcapDeviceManager.open(metadata.getName(), 10000);

			MacAddress original = null;
			Set<InetAddress> targetSet = null;
			synchronized (sync) {
				original = backups.remove(ip);
				targetSet = targets.remove(ip);
			}

			if (targetSet != null) {
				for (InetAddress target : targetSet) {
					send(device, arpCache, original, ip, target);
				}
			}
		} catch (IOException e) {
			logger.error("sleep proxy: cannot recover arp cache for ip " + ip.getHostAddress(), e);
		} finally {
			if (device != null)
				try {
					device.close();
				} catch (IOException e) {
				}
		}
	}

	private void send(PcapDevice device, Map<InetAddress, MacAddress> cache, MacAddress senderMac,
			InetAddress senderIp,
			InetAddress targetIp) {
		MacAddress targetMac = cache.get(targetIp);
		if (targetMac == null) {
			logger.warn("sleep proxy: mac not found, {} failed for target {}", senderIp.getHostAddress(),
					targetIp.getHostAddress());

			return;
		}

		ArpPacket p = ArpPacket.createRequest(senderMac, senderIp, targetMac, targetIp);
		EthernetHeader ethernetHeader = new EthernetHeader(senderMac, targetMac, EthernetType.ARP);
		EthernetFrame frame = new EthernetFrame(ethernetHeader, p.getBuffer());
		try {
			device.write(frame.getBuffer());
			logger.trace("sleep proxy: sent arp [src: {}, {}] to [{}, {}]", new Object[] { senderIp, senderMac,
					targetIp, targetMac });
		} catch (IOException e) {
			logger.error("sleep proxy: cannot send arp request for target {}, pretend {}", targetIp, senderIp);
		}
	}

	private class ArpUpdater implements Runnable {
		private volatile boolean stop;

		@Override
		public void run() {
			stop = false;
			while (!stop) {
				try {
					runOnce();
					Thread.sleep(15000);
				} catch (UnknownHostException e) {
				} catch (InterruptedException e) {
				}
			}
		}

		public void runOnce() throws UnknownHostException {
			buildArpCache();
		}

		public void close() {
			stop = true;
		}

		private void buildArpCache() throws UnknownHostException {
			PcapDeviceMetadata metadata = PcapDeviceManager.getDeviceMetadata(InetAddress.getByName("8.8.8.8"));
			Map<InetAddress, MacAddress> cache = null;
			try {
				InetAddress network = metadata.getSubnet();
				InetAddress mask = getMask(metadata);

				logger.debug("sleep proxy: arp scan for network {}, mask {}", network, mask);
				cache = Arping.scan(metadata.getName(), network, mask, 1000);
				for (InetAddress ip : cache.keySet()) {
					arpCache.put(ip, cache.get(ip));
				}

				logger.debug("sleep proxy: built arp cache, size {}, nic {}", cache.size(), metadata.getName());
			} catch (Exception e) {
				logger.error("sleep proxy: cannot build arp cache", e);
			}
		}

		private InetAddress getMask(PcapDeviceMetadata metadata) {
			int len = metadata.getNetworkPrefixLength();
			int n = 0;
			int mask = 1 << 31;

			for (int i = 0; i < len; i++) {
				n |= mask;
				mask >>= 1;
			}

			return IpConverter.toInetAddress(n);
		}

	}

	private class SynMonitor implements Runnable, TcpSegmentCallback {
		private PcapLiveRunner runner;

		public SynMonitor(PcapDevice device) {
			runner = new PcapLiveRunner(device);
			try {
				device.setFilter("tcp[tcpflags] & tcp-syn != 0");
			} catch (IOException e) {
				logger.error("sleep proxy: cannot set tcp filter");
			}
		}

		@Override
		public void run() {
			runner.getTcpDecoder().registerSegmentCallback(this);
			runner.run();
		}

		@Override
		public void onReceive(TcpSession session, TcpSegment segment) {
			InetAddress target = segment.getDestinationAddress();
			if (segment.isSyn() && backups.containsKey(target)) {
				logger.info("sleep proxy: syn packet found for {}", target.getHostAddress());
				MacAddress mac = backups.get(target);
				try {
					WakeOnLan.wake(mac);
					logger.info("sleep proxy: sent wol packet to {}, {}", target.getHostAddress(), mac);
				} catch (IOException e) {
					logger.error("sleep proxy: wake failed, {}, {}", target.getHostAddress(), mac);
				}
			}
		}

		public void close() {
			if (runner != null)
				runner.stop();
		}
	}
}

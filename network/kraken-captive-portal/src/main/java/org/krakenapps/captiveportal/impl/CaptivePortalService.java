/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.captiveportal.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.captiveportal.CaptivePortal;
import org.krakenapps.pcap.decoder.arp.ArpPacket;
import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.EthernetHeader;
import org.krakenapps.pcap.decoder.ethernet.EthernetType;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.util.Arping;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "captive-portal")
@Provides
public class CaptivePortalService implements CaptivePortal, Runnable {
	private static final String REDIRECT_IP_KEY = "redirect_ip";
	private static final String PCAP_DEVICE_KEY = "pcap_device";
	private static final int ARP_TIMEOUT = 3000;
	private static final String POISON_INTERVAL_KEY = "poison_interval";
	private static final String QUARANTINED_KEY = "quarantined";
	private static final String MIRRORING_KEY = "mirroring";
	private static final String GATEWAY_KEY = "gateway";

	private final Logger logger = LoggerFactory.getLogger(CaptivePortalService.class.getName());

	private String deviceName;
	private int poisonInterval;
	private boolean mirroringMode;
	private InetAddress redirectAddress;
	private InetAddress gatewayAddress;
	private MacAddress gatewayMac;
	private Set<InetAddress> quarantinedHosts;

	private FakeRouter fakeRouter;

	// arp poisoner
	private Thread poisonThread;
	private volatile boolean doStop;

	// ip-mac mappings
	private Map<InetAddress, IpMapping> arpCache;

	@Requires
	private PreferencesService prefsvc;

	@Validate
	public void start() throws BackingStoreException, IOException {
		quarantinedHosts = Collections.newSetFromMap(new ConcurrentHashMap<InetAddress, Boolean>());
		arpCache = new ConcurrentHashMap<InetAddress, IpMapping>();

		// loading
		Preferences root = prefsvc.getSystemPreferences();
		String redirectIp = root.get(REDIRECT_IP_KEY, null);
		if (redirectIp != null)
			this.redirectAddress = InetAddress.getByName(redirectIp);

		this.deviceName = root.get(PCAP_DEVICE_KEY, null);
		this.poisonInterval = root.getInt(POISON_INTERVAL_KEY, 10000);
		this.mirroringMode = root.getBoolean(MIRRORING_KEY, false);
		this.gatewayAddress = InetAddress.getByName(root.get(GATEWAY_KEY, null));

		Preferences node = root.node(QUARANTINED_KEY);
		for (String ip : node.childrenNames()) {
			quarantinedHosts.add(InetAddress.getByName(ip));
		}

		// fake router
		if (deviceName != null) {
			fakeRouter = new FakeRouter(deviceName, this);
			fakeRouter.start();
		}

		// thread start
		startPoisoner();

		logger.info("kraken captive portal: poisoning thread started");
	}

	@Invalidate
	public void stop() {
		quarantinedHosts.clear();

		stopPoisoner();
		fakeRouter.stop();
	}

	private void startPoisoner() {
		doStop = false;
		poisonThread = new Thread(this, "Captive Portal ARP Poisoner");
		poisonThread.start();
	}

	private void stopPoisoner() {
		doStop = true;
		poisonThread.interrupt();
	}

	@Override
	public InetAddress getRedirectAddress() {
		return redirectAddress;
	}

	@Override
	public void setRedirectAddress(InetAddress ip) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			root.put(REDIRECT_IP_KEY, ip.getHostAddress());
			root.flush();
			root.sync();

			this.redirectAddress = ip;
		} catch (BackingStoreException e) {
			logger.error("kraken captive portal: cannot set redirect ip", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<InetAddress, MacAddress> getArpCache() {
		Map<InetAddress, MacAddress> m = new HashMap<InetAddress, MacAddress>();
		for (InetAddress ip : arpCache.keySet()) {
			IpMapping mapping = arpCache.get(ip);
			m.put(ip, mapping.mac);
		}

		return m;
	}

	@Override
	public MacAddress getQuarantinedMac(InetAddress ip) {
		IpMapping mapping = arpCache.get(ip);
		if (mapping == null)
			return null;

		return mapping.mac;
	}

	@Override
	public void run() {
		try {
			while (!doStop) {
				try {
					if (deviceName != null)
						spoof();

					Thread.sleep(poisonInterval);
				} catch (IOException e) {
					logger.error("kraken captive portal: io error", e);
				}
			}
		} catch (InterruptedException e) {
			logger.error("kraken captive portal: poisoning thread interrupted");
		} catch (Exception e) {
			logger.error("kraken captive portal: poisoning thread error", e);
		} finally {
			logger.info("kraken captive portal: poisoning thread stopped");
		}
	}

	@Override
	public void spoof() throws IOException {
		// ensure gateway mac address
		MacAddress mac = Arping.query(gatewayAddress, ARP_TIMEOUT);
		arpCache.put(gatewayAddress, new IpMapping(mac));

		// spoof quarantined hosts
		for (InetAddress ip : quarantinedHosts) {
			try {
				spoof(ip);
			} catch (IOException e) {
				logger.error("kraken captive portal: pcap error", e);
			}
		}
	}

	private void spoof(InetAddress targetIp) throws IOException {
		// get gateway mac address
		gatewayMac = arpCache.get(gatewayAddress).mac;

		// get target host mac address
		IpMapping mapping = getTargetMac(targetIp);
		if (mapping == null)
			return;

		MacAddress targetMac = mapping.mac;
		PcapDevice device = PcapDeviceManager.open(deviceName, ARP_TIMEOUT);

		try {
			// spoof host (as gateway)
			sendArpRequest(device, device.getMetadata().getMacAddress(), gatewayAddress, targetMac, targetIp);

			// spoof gateway (as host)
			sendArpRequest(device, device.getMetadata().getMacAddress(), targetIp, gatewayMac, gatewayAddress);
		} finally {
			device.close();
		}
	}

	private void unspoof(InetAddress targetIp) throws IOException {
		// get gateway mac address
		gatewayMac = arpCache.get(gatewayAddress).mac;

		// get target host mac address
		IpMapping mapping = getTargetMac(targetIp);
		if (mapping == null)
			return;

		MacAddress targetMac = mapping.mac;

		// remove from arp cache
		arpCache.remove(targetIp);

		// recover host arp cache
		PcapDevice device = PcapDeviceManager.open(deviceName, ARP_TIMEOUT);
		sendArpRequest(device, gatewayMac, gatewayAddress, targetMac, targetIp);
		sendArpRequest(device, targetMac, targetIp, gatewayMac, gatewayAddress);
	}

	private IpMapping getTargetMac(InetAddress targetIp) throws IOException {
		IpMapping mapping = arpCache.get(targetIp);
		if (mapping == null || isTimeout(mapping.updated)) {
			MacAddress mac = Arping.query(targetIp, ARP_TIMEOUT);
			if (mac == null) {
				logger.trace("kraken captive portal: host not found for {}", targetIp);
				return null;
			} else {
				mapping = new IpMapping(mac);
				arpCache.put(targetIp, mapping);
			}
		}

		return mapping;
	}

	private void sendArpRequest(PcapDevice device, MacAddress senderMac, InetAddress senderIp, MacAddress targetMac,
			InetAddress targetIp) {
		MacAddress deviceMac = device.getMetadata().getMacAddress();
		ArpPacket p = ArpPacket.createRequest(senderMac, senderIp, new MacAddress("00:00:00:00:00:00"), targetIp);
		EthernetHeader ethernetHeader = new EthernetHeader(deviceMac, targetMac, EthernetType.ARP);
		EthernetFrame frame = new EthernetFrame(ethernetHeader, p.getBuffer());

		try {
			logger.debug("kraken captive portal: send arp, sender [mac: {}, ip: {}], target [mac: {}, ip: {}]",
					new Object[] { senderMac, senderIp, targetMac, targetIp });
			device.write(frame.getBuffer());
		} catch (IOException e) {
			logger.error("kraken captive portal: arp request error", e);
		}
	}

	private boolean isTimeout(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MILLISECOND, ARP_TIMEOUT);
		Date expire = c.getTime();
		Date now = new Date();
		return now.after(expire);
	}

	@Override
	public String getPcapDeviceName() {
		return deviceName;
	}

	@Override
	public void setPcapDeviceName(String name) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			root.put(PCAP_DEVICE_KEY, name);
			root.flush();
			root.sync();

			this.deviceName = name;

			// restart fake router
			fakeRouter.stop();
			fakeRouter = new FakeRouter(name, this);
			logger.info("kraken captive portal: fake router restarted");

			// restart poisoner
			stopPoisoner();
			startPoisoner();
			logger.info("kraken captive portal: arp poisoner restarted");
		} catch (BackingStoreException e) {
			logger.error("kraken captive portal: cannot set pcap device name", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.error("kraken captive portal: fake router setting error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getPoisonInterval() {
		return poisonInterval;
	}

	@Override
	public void setPoisonInterval(int milliseconds) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			root.putInt(POISON_INTERVAL_KEY, milliseconds);
			root.flush();
			root.sync();

			this.poisonInterval = milliseconds;
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MacAddress getGatewayMacAddress() {
		return gatewayMac;
	}

	@Override
	public InetAddress getGatewayAddress() {
		return gatewayAddress;
	}

	@Override
	public void setGatewayAddress(InetAddress address) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			root.put(GATEWAY_KEY, address.getHostAddress());
			root.flush();
			root.sync();

			this.gatewayAddress = address;
		} catch (BackingStoreException e) {
			logger.error("kraken captive portal: cannot set gateway address", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getMirroringMode() {
		return mirroringMode;
	}

	@Override
	public void setMirroringMode(boolean mirroringMode) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			root.putBoolean(MIRRORING_KEY, mirroringMode);
			root.flush();
			root.sync();

			this.mirroringMode = mirroringMode;
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<InetAddress> getQuarantinedHosts() {
		return new ArrayList<InetAddress>(quarantinedHosts);
	}

	@Override
	public void quarantineHost(InetAddress address) throws IOException {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			Preferences p = root.node(QUARANTINED_KEY);

			p.node(address.getHostAddress());

			root.flush();
			root.sync();

			quarantinedHosts.add(address);

			spoof(address);
		} catch (BackingStoreException e) {
			logger.error("kraken captive portal: cannot quarantine host " + address.getHostAddress(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void unquarantineHost(InetAddress address) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			Preferences p = root.node(QUARANTINED_KEY);

			p.node(address.getHostAddress()).removeNode();
			root.flush();
			root.sync();

			quarantinedHosts.remove(address);

			unspoof(address);
		} catch (BackingStoreException e) {
			logger.error("kraken captive portal: cannot unquarantine host " + address.getHostAddress(), e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.error("kraken captive portal: cannot recover arp cache for " + address.getHostAddress(), e);
			throw new RuntimeException(e);
		}
	}

	private static class IpMapping {
		private MacAddress mac;
		private Date updated;

		public IpMapping(MacAddress mac) {
			this.mac = mac;
			this.updated = new Date();
		}
	}
}

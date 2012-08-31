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
package org.krakenapps.snmpmon;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.SimpleLog;
import org.krakenapps.snmpmon.Interface.IfEntry;
import org.krakenapps.snmpmon.SnmpQueryLoggerFactory.ConfigOption;
import org.slf4j.Logger;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeListener;
import org.snmp4j.util.TreeUtils;

/**
 * @author stania
 */
public class SnmpQueryLogger extends AbstractLogger {
	private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	private static final int ethernetCsmacd = 6;
	private static final int timeout = 3000;

	private static ExecutorService sender;

	private Snmp snmp;
	private CommunityTarget commTarget;
	private PDU pdu;
	private Integer ifNumber;

	private long loggerCreated;
	private SnmpAgent target;

	public static void open() {
		BlockingQueue<Runnable> workQueue = new PriorityBlockingQueue<Runnable>(11, new Comparator<Runnable>() {
			@Override
			public int compare(Runnable o1, Runnable o2) {
				if (o1 == null || !(o1 instanceof SnmpQueryLogger))
					return -1;
				if (o2 == null || !(o2 instanceof SnmpQueryLogger))
					return 1;
				SnmpQueryLogger l1 = (SnmpQueryLogger) o1;
				if (l1.getLastRunDate() == null)
					return -1;
				SnmpQueryLogger l2 = (SnmpQueryLogger) o2;
				if (l2.getLastRunDate() == null)
					return 1;
				boolean result = (l1.getLastRunDate().getTime() + l1.getInterval()) < (l2.getLastRunDate().getTime() + l2
						.getInterval());
				return result ? -1 : 1;
			}
		});

		if (sender != null)
			sender.shutdownNow();
		sender = new ThreadPoolExecutor(1, 1, 1, TimeUnit.DAYS, workQueue, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "Snmp Query Logger");
			}
		});
	}

	public static void close() {
		sender.shutdownNow();
		sender = null;
	}

	public SnmpQueryLogger(String hostGuid, String name, String description, LoggerFactory loggerFactory, Properties config) {
		super(hostGuid, name, description, loggerFactory, config);
		parseConfig(config);
		init();
	}

	public SnmpQueryLogger(String hostGuid, String name, String description, LoggerFactory loggerFactory, SnmpAgent agent) {
		super(hostGuid, name, description, loggerFactory);
		target = agent;
		init();
	}

	private void init() {
		loggerCreated = new Date().getTime();

		commTarget = new CommunityTarget();
		commTarget.setCommunity(new OctetString(target.getCommunity()));
		commTarget.setVersion(getSnmpVersionContstant(target.getSnmpVersion()));
		commTarget.setAddress(new UdpAddress(target.getIp() + "/" + Integer.toString(target.getPort())));
		commTarget.setRetries(2);
		commTarget.setTimeout(timeout);
	}

	private int getSnmpVersionContstant(int snmpVersion) {
		switch (snmpVersion) {
		case 1:
			return SnmpConstants.version1;
		case 2:
			return SnmpConstants.version2c;
		case 3:
			return SnmpConstants.version3;
		default:
			return SnmpConstants.version2c;
		}
	}

	private void parseConfig(Properties config) {
		if (logger.isDebugEnabled()) {
			for (Object key : config.keySet())
				logger.debug("kraken snmpmon: logger option key [{}], value [{}]", key, config.get(key));
		}

		String ip = (String) config.get(ConfigOption.AgentIP.getConfigKey());
		if (ip == null)
			throw new RuntimeException("target address cannot be null.");

		int port = 161;
		if (config.containsKey(ConfigOption.AgentPort.getConfigKey()))
			port = Integer.valueOf((String) config.get(ConfigOption.AgentPort.getConfigKey()));

		target = new SnmpAgent();
		target.setIp(ip);
		target.setPort(port);

		// parse community strings
		target.setCommunity((String) config.get(SnmpQueryLoggerFactory.ConfigOption.SnmpCommunity.getConfigKey()));

		// parse versions
		String version = config.getProperty(SnmpQueryLoggerFactory.ConfigOption.SnmpVersion.getConfigKey());
		target.setSnmpVersion(getSnmpVersion(version));
	}

	private int getSnmpVersion(String version) {
		if (version != null && Integer.valueOf(version) == 1) {
			return SnmpConstants.version1;
		} else {
			return SnmpConstants.version2c;
		}
	}

	public void setSnmp(Snmp snmp) {
		this.snmp = snmp;
	}

	@Override
	protected void runOnce() {
		try {
			query();
		} catch (Exception e) {
			String targetStr = String.format("for %s:%d", target.getIp(), target.getPort());
			if (e instanceof RuntimeException) {
				if (e.getCause() != null) {
					logger.warn("SNMP query failed " + targetStr + ", cause: " + e.getCause().getMessage());
					logger.debug("SNMP query failed exception detail", e);
				} else {
					if (!(e instanceof TimeoutException)) {
						logger.warn("SNMP query failed " + targetStr + ", msg: " + e.getClass().getName() + "(" + e.getMessage()
								+ ")");
						logger.debug("SNMP query failed exception detail", e);
					}
				}

				if (e instanceof TimeoutException) {
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("source_ip", deviceIp);
					m.put("target_ip", target.getIp());
					m.put("proto", "snmp");
					m.put("port", 161);
					m.put("timeout", timeout);
					// Log on error
					Log log = new SimpleLog(new Date(), getFullName(), "heartbeat", "Heartbeat failed", m);
					write(log);
				}
			} else {
				logger.warn("SNMP query failed " + targetStr + ": " + e.getClass().getName() + ":" + e.getMessage());
				logger.debug("SNMP query failed exception detail", e);
			}
		}
	}

	private ResponseListener listener = new ResponseListener() {
		@SuppressWarnings("unchecked")
		@Override
		public void onResponse(ResponseEvent event) {
			if (event.getUserObject() != null) {
				try {
					((Snmp) event.getUserObject()).close();
				} catch (IOException e) {
				}
			}

			if (event == null || event.getResponse() == null)
				return;

			List<VariableBinding> results = event.getResponse().getVariableBindings();
			if (results == null)
				return;

			parseResult(results);
		}
	};

	private void parseResult(List<VariableBinding> results) {
		VariableBinding vbIfNumber = results.get(0);
		String oidIfNumber = Interface.oidInterfaces + ".1.0";
		if (!vbIfNumber.getOid().equals(new OID(oidIfNumber)))
			throw new ParseException("first result doesn't match with " + oidIfNumber + ": " + vbIfNumber.getOid());

		int ifNumber = vbIfNumber.getVariable().toInt();
		if (this.ifNumber == null)
			this.ifNumber = ifNumber;
		else {
			if (this.ifNumber != ifNumber)
				pdu = null;
		}

		Map<Integer, Interface> interfaces = new HashMap<Integer, Interface>();

		// building interfaceImpl instances
		for (int i = 0; i < ifNumber; ++i) {
			VariableBinding vb = results.get(1 + i);
			if (!vb.getOid().startsWith(Interface.IfEntry.ifIndex.getOID()))
				throw new ParseException("invalid parse value while building interfaces: " + vb);
			int ifIndex = vb.getVariable().toInt();
			interfaces.put(ifIndex, new Interface(ifIndex));
		}

		// set properties for interfaces
		for (VariableBinding vb : results.subList(1 + ifNumber, results.size())) {
			OID oid = vb.getOid();

			OID parent = (OID) oid.clone();
			int ifIndex = parent.removeLast();
			IfEntry ifEntry = IfEntry.valueOf(parent);
			if (ifEntry == null)
				continue;

			Variable var = vb.getVariable();
			Interface iface = interfaces.get(ifIndex);
			if (iface == null) {
				continue;
			} else {
				// filter non-etherCsmacd
				iface.setProperty(ifEntry, var);
				if (ifEntry.equals(IfEntry.ifType)) {
					if (var.toInt() != ethernetCsmacd) {
						interfaces.remove(ifIndex);
					}
				}
			}
		}

		Map<String, Object> data = new HashMap<String, Object>();
		Map<OldValueKey, Long> newOldValues = new HashMap<OldValueKey, Long>();
		try {
			Date now = new Date();

			// calculate interval
			OldValueKey intervalKey = new OldValueKey(target, -1, IfEntry.INTERVAL);
			newOldValues.put(intervalKey, now.getTime());
			long interval;
			Long oldInterval = oldValues.get(intervalKey);
			if (oldInterval == null)
				interval = delta32(now.getTime(), loggerCreated);
			else
				interval = delta32(now.getTime(), oldInterval.longValue());

			TrafficSummary max_ts = new TrafficSummary(' ', -1, -1, -1);
			String max_descr = null;
			String max_physAddress = null;
			// build & write logs for each interfaces
			for (Entry<Integer, Interface> entry : interfaces.entrySet()) {
				Interface iface = entry.getValue();
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("scope", "device");
				for (Entry<IfEntry, Object> ve : iface.getProperties().entrySet()) {
					Variable value = (Variable) ve.getValue();
					if (value instanceof Counter32) {
						OldValueKey key = new OldValueKey(target, entry.getKey(), ve.getKey());
						newOldValues.put(key, value.toLong());
						Long oldValue = oldValues.get(key);
						if (oldValue == null) {
							value = new UnsignedInteger32(0);
						} else {
							// to calc sanitized delta..
							long delta = delta32(value.toLong(), oldValue.longValue());
							value = new UnsignedInteger32(delta);
						}
					}
					String logParamKey = logParamKeys.get(ve.getKey());
					if (logParamKey == null)
						logParamKey = ve.getKey().toString();
					params.put(logParamKey, convertVariableToPrimitive(value));
				}

				// interval in milliseconds
				params.put("interval", interval);

				String descr = iface.getProperties().get(IfEntry.ifDescr).toString();
				String physAddress = iface.getProperties().get(IfEntry.ifPhysAddress).toString();

				TrafficSummary rts = getTrafficSummary('R', params);
				TrafficSummary tts = getTrafficSummary('T', params);

				if (params.get(logParamKeys.get(IfEntry.ifOperStatus)).toString().equals("1")) {
					if (max_ts.bps < rts.bps) {
						max_ts = rts;
						max_descr = descr;
						max_physAddress = physAddress;
					}
					if (max_ts.bps < tts.bps) {
						max_ts = tts;
						max_descr = descr;
						max_physAddress = physAddress;
					}
				}

				params.put("msg", getMessage(descr, physAddress, rts, tts));
				data.put(descr, params);
			}

			// max log
			Map<String, Object> m = new HashMap<String, Object>();
			String totalMsg = null;

			if (max_ts.utilization < 0) {
				logger.warn("SnmpMonitor max_ts.utilization < 0 ({}, {}, {})", new Object[] { max_ts.utilization, max_ts.bps,
						max_descr });
				m.put("scope", "total");
				m.put("max_usage", 0);
				m.put("description", max_descr);
				m.put("mac", max_physAddress);

				totalMsg = String.format("network usage: max network usage [%s (%s) - %d%%]", max_descr, max_physAddress,
						max_ts.utilization);
				m.put("msg", totalMsg);

				data.put("_total", m);
			} else {
				m.put("scope", "total");
				m.put("max_usage", max_ts.utilization);
				m.put("description", max_descr);
				m.put("mac", max_physAddress);

				totalMsg = String.format("network usage: max network usage [%s (%s) - %d%%]", max_descr, max_physAddress,
						max_ts.utilization);
				m.put("msg", totalMsg);

				data.put("_total", m);
			}

			Log log = new SimpleLog(new Date(), getFullName(), "network-usage", totalMsg, data);
			write(log);
		} finally {
			oldValues = newOldValues;
		}
	}

	private void query() throws Exception {
		Snmp snmp = this.snmp;
		if (snmp == null) {
			TransportMapping transport = new DefaultUdpTransportMapping(new UdpAddress());
			transport.listen();
			snmp = new Snmp(transport);
		}

		if (pdu != null) {
			snmp.getBulk(pdu, commTarget, (this.snmp == null) ? snmp : null, listener);
		} else {
			ifNumber = null;
			pdu = new PDU();
			pdu.add(new VariableBinding(new OID(Interface.oidInterfaces)));
			pdu.setType(PDU.GETBULK);
			pdu.setMaxRepetitions(10);

			TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
			treeUtils.getSubtree(commTarget, Interface.oidInterfaces, (this.snmp == null) ? snmp : null, new TreeListener() {
				private List<VariableBinding> results = new ArrayList<VariableBinding>();
				private boolean finish = false;

				@Override
				public boolean next(TreeEvent event) {
					if (event.getVariableBindings() != null) {
						pdu.addAllOIDs(event.getVariableBindings());
						for (VariableBinding vb : event.getVariableBindings())
							results.add(vb);
					}
					return true;
				}

				@Override
				public void finished(TreeEvent event) {
					finish = true;
					if (event.getVariableBindings() != null) {
						pdu.addAllOIDs(event.getVariableBindings());
						for (VariableBinding vb : event.getVariableBindings())
							results.add(vb);
					}
					if (event.getUserObject() != null) {
						try {
							((Snmp) event.getUserObject()).close();
						} catch (IOException e) {
						}
					}
					parseResult(results);
				}

				@Override
				public boolean isFinished() {
					return finish;
				}
			});
		}
	}

	@Override
	protected ExecutorService getExecutor() {
		return sender;
	}

	private static String deviceIp = null;
	static {
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				if (networkInterface.getHardwareAddress() == null || networkInterface.getHardwareAddress().length != 6)
					continue;
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					if (inetAddress.getHostAddress().startsWith("127."))
						continue;
					deviceIp = inetAddress.getHostAddress();
				}
			}
			if (deviceIp == null)
				deviceIp = InetAddress.getLocalHost().getHostAddress();
		} catch (SocketException e) {
		} catch (UnknownHostException e) {
		}
	}

	private static class OldValueKey {
		private SnmpAgent agent;
		private int ifIndex;
		private IfEntry ifEntry;

		public OldValueKey(SnmpAgent agent, int ifIndex, IfEntry ifEntry) {
			this.agent = agent;
			this.ifIndex = ifIndex;
			this.ifEntry = ifEntry;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((agent == null) ? 0 : agent.hashCode());
			result = prime * result + ((ifEntry == null) ? 0 : ifEntry.hashCode());
			result = prime * result + ifIndex;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OldValueKey other = (OldValueKey) obj;
			if (agent == null) {
				if (other.agent != null)
					return false;
			} else if (!agent.equals(other.agent))
				return false;
			if (ifEntry != other.ifEntry)
				return false;
			if (ifIndex != other.ifIndex)
				return false;
			return true;
		}
	}

	private Map<OldValueKey, Long> oldValues = new HashMap<OldValueKey, Long>();

	private static EnumMap<IfEntry, String> logParamKeys = new EnumMap<IfEntry, String>(IfEntry.class);
	static {
		logParamKeys.put(IfEntry.ifIndex, "index");
		logParamKeys.put(IfEntry.ifType, "type");
		logParamKeys.put(IfEntry.ifDescr, "description");
		logParamKeys.put(IfEntry.ifMtu, "mtu");
		logParamKeys.put(IfEntry.ifPhysAddress, "mac");
		logParamKeys.put(IfEntry.ifSpeed, "bandwidth");
		logParamKeys.put(IfEntry.ifOperStatus, "oper_status");
		logParamKeys.put(IfEntry.ifAdminStatus, "admin_status");
		logParamKeys.put(IfEntry.ifSpecific, "specific");
		logParamKeys.put(IfEntry.ifLastChange, "last_change");
		logParamKeys.put(IfEntry.ifInOctets, "rx_bytes_delta");
		logParamKeys.put(IfEntry.ifOutOctets, "tx_bytes_delta");
		logParamKeys.put(IfEntry.ifInUcastPkts, "rx_ucast_pkts_delta");
		logParamKeys.put(IfEntry.ifOutUcastPkts, "tx_ucast_pkts_delta");
		logParamKeys.put(IfEntry.ifInNUcastPkts, "rx_nucast_pkts_delta");
		logParamKeys.put(IfEntry.ifOutNUcastPkts, "tx_nucast_pkts_delta");
		logParamKeys.put(IfEntry.ifInErrors, "rx_errors_delta");
		logParamKeys.put(IfEntry.ifOutErrors, "tx_errors_delta");
		logParamKeys.put(IfEntry.ifInDiscards, "rx_discards_delta");
		logParamKeys.put(IfEntry.ifOutDiscards, "tx_discards_delta");
		logParamKeys.put(IfEntry.ifInUnknownProtos, "rx_unknown_protos");
		logParamKeys.put(IfEntry.ifOutQLen, "tx_queue_length");
	}

	private long delta32(long nv, long ov) {
		if (nv < ov) {
			return nv + 0xFFFFFFFFL - ov;
		} else {
			return nv - ov;
		}
	}

	private <T extends Variable> Object convertVariableToPrimitive(T value) {
		Class<? extends Variable> cls = value.getClass();
		if (cls.equals(Counter64.class))
			return value.toLong();
		else if (cls.equals(UnsignedInteger32.class))
			return value.toLong();
		else if (cls.equals(Counter32.class))
			return value.toLong();
		else if (cls.equals(Gauge32.class))
			return value.toLong();
		else if (cls.equals(TimeTicks.class))
			return value.toLong();
		else if (cls.equals(Integer32.class))
			return value.toInt();
		return value.toString();
	}

	private TrafficSummary getTrafficSummary(char type, Map<String, Object> params) {
		String octets = (type == 'R') ? logParamKeys.get(IfEntry.ifInOctets) : logParamKeys.get(IfEntry.ifOutOctets);
		String ucastPkts = (type == 'R') ? logParamKeys.get(IfEntry.ifInUcastPkts) : logParamKeys.get(IfEntry.ifOutUcastPkts);
		String nucastPkts = (type == 'R') ? logParamKeys.get(IfEntry.ifInNUcastPkts) : logParamKeys.get(IfEntry.ifOutNUcastPkts);

		long octet_delta = ((Number) params.get(octets)).longValue();
		long pkt_delta = 0;
		if (params.containsKey(ucastPkts)) {
			pkt_delta += ((Number) params.get(ucastPkts)).longValue();
		}
		if (params.containsKey(nucastPkts)) {
			pkt_delta += ((Number) params.get(nucastPkts)).longValue();
		}
		long bandwidth = ((Number) params.get(logParamKeys.get(IfEntry.ifSpeed))).longValue();
		long interval = (Long) params.get("interval");

		int utilization;
		int bps = (int) ((double) octet_delta / interval * 8.0 * 1000);
		int fps = (int) ((double) pkt_delta / interval * 1000);
		if (bandwidth == 0)
			utilization = 0;
		else
			utilization = (int) ((long) bps * 100L / bandwidth);
		if (utilization < 0) {
			logger.warn("minus utilization: {}, {}, {}, {}", new Object[] { utilization, bps, fps, bandwidth });
		}
		if (utilization > 100) {
			logger.warn("max_ts utilization > 100 ! ({})", utilization);
			logger.warn(String.format(
					"ifIndex: %d, physAddress: %s, interval(ms): %d, octet_delta: %d, pkt_delta: %d, bandwidth: %d",
					(Number) params.get(logParamKeys.get(IfEntry.ifIndex)),
					(String) params.get(logParamKeys.get(IfEntry.ifPhysAddress)), interval, octet_delta, pkt_delta, bandwidth));
		}

		return new TrafficSummary(type, utilization, bps, fps);
	}

	private String getMessage(String descr, String physAddress, TrafficSummary rts, TrafficSummary tts) {
		return String.format("%s (%s), %s, %s", descr, physAddress, rts.toString(), tts.toString());
	}

	private static class TrafficSummary {
		public char type;
		public int utilization;
		public int bps;
		public int fps;

		public TrafficSummary(char type, int utilization, int bps, int fps) {
			this.type = type;
			this.utilization = utilization;
			this.bps = bps;
			this.fps = fps;
		}

		@Override
		public String toString() {
			return String.format("%cX[%d%% %s, %dfps]", type, utilization, bpsToString(bps), fps);
		}

		private static String[] bpsUnits = new String[] { "", "K", "M", "G", "T", "P" };

		private static String bpsToString(int bps) {
			int mul = 0;
			while (bps >= 1000) {
				bps = bps / 1000;
				mul++;
			}
			String bpsUnit = bpsUnits[mul];

			return String.format("%d%sbps", bps, bpsUnit);
		}
	}
}
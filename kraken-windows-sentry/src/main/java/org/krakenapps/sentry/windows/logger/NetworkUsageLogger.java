/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.sentry.windows.logger;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.SimpleLog;
import org.krakenapps.winapi.PerformanceCounter;
import org.slf4j.Logger;

public class NetworkUsageLogger extends AbstractLogger {
	private final Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkUsageLogger.class.getName());

	private long[] lastRxErrors;
	private long[] lastTxErrors;
	private long[] lastRxDiscards;
	private long[] lastTxDiscards;
	private long lastCheckTime;

	public NetworkUsageLogger(LoggerSpecification spec, LoggerFactory loggerFactory) {
		super(spec.getName(), spec.getDescription(), loggerFactory);

		List<String> instances = PerformanceCounter.getInstances("Network Interface");
		int length = instances.size();

		lastRxErrors = new long[length];
		lastTxErrors = new long[length];
		lastRxDiscards = new long[length];
		lastTxDiscards = new long[length];
		lastCheckTime = new Date().getTime();
	}

	@Override
	protected void runOnce() {
		try {
			check();
		} catch (InterruptedException e) {
		}
	}

	private void check() throws InterruptedException {
		List<String> instances = PerformanceCounter.getInstances("Network Interface");

		Counters[] countersArray = new Counters[instances.size()];
		int i = 0;
		for (String instance : instances) {
			countersArray[i] = new Counters();
			countersArray[i].instanceName = instance;
			i++;
		}

		try {
			openAll(countersArray);
			Thread.sleep(1000);
			checkAll(countersArray);
		} catch (Exception e) {
			logger.warn("kraken windows sentry: check nic usage failed", e);
		} finally {
			closeAll(countersArray);
		}
	}

	private PerformanceCounter open(String counter, String instanceName) {
		return new PerformanceCounter("Network Interface", counter, instanceName);
	}

	private void closeAll(Counters[] countersArray) {
		for (int i = 0; i < countersArray.length; i++) {
			Counters c = countersArray[i];

			close(c.bandwidth);
			close(c.inBytesPerSec);
			close(c.outBytesPerSec);
			close(c.inUcastPktsPerSec);
			close(c.outUcastPktsPerSec);
			close(c.inNUcastPktsPerSec);
			close(c.outNUcastPktsPerSec);
			close(c.inErrors);
			close(c.outErrors);
			close(c.inDiscards);
			close(c.outDiscards);
		}
	}

	private void close(PerformanceCounter c) {
		if (c != null)
			try {
				c.close();
			} catch (Exception e) {
			}
	}

	private void openAll(Counters[] countersArray) {
		for (int i = 0; i < countersArray.length; i++) {
			Counters c = countersArray[i];

			String instanceName = c.instanceName;
			String macAddress = getMacAddress(instanceName);
			if (isFiltered(instanceName, macAddress))
				continue;

			c.bandwidth = open("Current Bandwidth", instanceName);
			c.inBytesPerSec = open("Bytes Received/sec", instanceName);
			c.outBytesPerSec = open("Bytes Sent/sec", instanceName);
			c.inUcastPktsPerSec = open("Packets Received Unicast/sec", instanceName);
			c.outUcastPktsPerSec = open("Packets Sent Unicast/sec", instanceName);
			c.inNUcastPktsPerSec = open("Packets Received Non-Unicast/sec", instanceName);
			c.outNUcastPktsPerSec = open("Packets Sent Non-Unicast/sec", instanceName);
			c.inErrors = open("Packets Received Errors", instanceName);
			c.outErrors = open("Packets Outbound Errors", instanceName);
			c.inDiscards = open("Packets Received Discarded", instanceName);
			c.outDiscards = open("Packets Outbound Discarded", instanceName);

			// set baseline
			c.inBytesPerSec.nextValue();
			c.outBytesPerSec.nextValue();
			c.inUcastPktsPerSec.nextValue();
			c.outUcastPktsPerSec.nextValue();
			c.inNUcastPktsPerSec.nextValue();
			c.outNUcastPktsPerSec.nextValue();
		}
	}

	public void checkAll(Counters[] countersArray) throws InterruptedException {
		List<Log> logs = new ArrayList<Log>();

		long now = new Date().getTime();
		long interval = now - lastCheckTime;
		int seconds = Math.round(interval / 1000);
		lastCheckTime = now;

		for (int i = 0; i < countersArray.length; i++) {
			Counters c = countersArray[i];
			String instanceName = c.instanceName;
			String macAddress = getMacAddress(instanceName);

			logger.trace("kraken windows sentry: nic counter instance [{}], mac [{}]", instanceName, macAddress);
			if (isFiltered(instanceName, macAddress)) {
				logger.trace("kraken windows sentry: filtered nic counter instance [{}], mac [{}]", instanceName,
						macAddress);
				continue;
			}

			// fetch all
			long rxBytesDelta = Math.round(c.inBytesPerSec.nextValue() * seconds);
			long txBytesDelta = Math.round(c.outBytesPerSec.nextValue() * seconds);
			long rxUcastPktsDelta = Math.round(c.inUcastPktsPerSec.nextValue() * seconds);
			long txUcastPktsDelta = Math.round(c.outUcastPktsPerSec.nextValue() * seconds);
			long rxNUcastPktsDelta = Math.round(c.inNUcastPktsPerSec.nextValue() * seconds);
			long txNUcastPktsDelta = Math.round(c.outNUcastPktsPerSec.nextValue() * seconds);

			long rxErrors = (long) c.inErrors.nextValue();
			long txErrors = (long) c.outErrors.nextValue();
			long rxDiscards = (long) c.inDiscards.nextValue();
			long txDiscards = (long) c.outDiscards.nextValue();

			long rxErrorsDelta = rxErrors - lastRxErrors[i];
			long txErrorsDelta = txErrors - lastTxErrors[i];
			long rxDiscardsDelta = rxDiscards - lastRxDiscards[i];
			long txDiscardsDelta = txDiscards - lastTxDiscards[i];

			lastRxErrors[i] = rxErrors;
			lastTxErrors[i] = txErrors;
			lastRxDiscards[i] = rxDiscards;
			lastTxDiscards[i] = txDiscards;

			long bandwidth = (long) c.bandwidth.nextValue();

			Map<String, Object> data = new HashMap<String, Object>();

			data.put("scope", "device");
			data.put("interval", interval);
			data.put("index", i);
			data.put("type", getType(instanceName));
			data.put("description", instanceName);
			data.put("mtu", getMtu(instanceName));
			data.put("mac", macAddress);
			data.put("bandwidth", bandwidth);
			data.put("rx_bytes_delta", rxBytesDelta);
			data.put("tx_bytes_delta", txBytesDelta);
			data.put("rx_ucast_pkts_delta", rxUcastPktsDelta);
			data.put("tx_ucast_pkts_delta", txUcastPktsDelta);
			data.put("rx_nucast_pkts_delta", rxNUcastPktsDelta);
			data.put("tx_nucast_pkts_delta", txNUcastPktsDelta);
			data.put("rx_errors_delta", rxErrorsDelta);
			data.put("tx_errors_delta", txErrorsDelta);
			data.put("rx_discards_delta", rxDiscardsDelta);
			data.put("tx_discards_delta", txDiscardsDelta);

			long rxBps = rxBytesDelta * 8 / seconds;
			long txBps = txBytesDelta * 8 / seconds;
			long rxFps = (rxUcastPktsDelta + rxNUcastPktsDelta) * 8 / seconds;
			long txFps = (txUcastPktsDelta + txNUcastPktsDelta) * 8 / seconds;

			int rxUsage = 0;
			int txUsage = 0;

			if (bandwidth > 0) {
				rxUsage = (int) (rxBps * 100 / bandwidth);
				txUsage = (int) (txBps * 100 / bandwidth);
			}

			String rxBpsText = addUnit(rxBps, "bps");
			String txBpsText = addUnit(txBps, "bps");
			String rxFpsText = addUnit(rxFps, "fps");
			String txFpsText = addUnit(txFps, "fps");

			String msg = String.format("network usage: %s (%s), RX[%d%%, %s, %s], TX[%d%%, %s, %s]", instanceName,
					macAddress, rxUsage, rxBpsText, rxFpsText, txUsage, txBpsText, txFpsText);

			Log log = new SimpleLog(new Date(), getFullName(), "device", msg, data);
			write(log);

			logs.add(log);
		}

		Log max = findMax(logs);
		String description;
		String mac;
		int networkUsage;

		if (max != null) {
			description = (String) max.getParams().get("description");
			mac = (String) max.getParams().get("mac");
			networkUsage = getNetworkUsage(max);
		} else {
			description = "-";
			mac = "-";
			networkUsage = 0;
		}

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("scope", "total");
		m.put("max_usage", networkUsage);
		m.put("description", description);
		m.put("mac", mac);

		String msg = String.format("network usage: max network usage [%s (%s) - %d%%]", description, mac, networkUsage);
		Log log = new SimpleLog(new Date(), getFullName(), "total", msg, m);
		write(log);
	}

	private Log findMax(List<Log> logs) {
		Log selected = null;
		int maxUsage = 0;

		for (Log log : logs) {
			int usage = getNetworkUsage(log);
			if (usage >= maxUsage) {
				selected = log;
				maxUsage = usage;
			}
		}

		return selected;
	}

	private int getNetworkUsage(Log log) {
		int seconds = Math.round((Long) log.getParams().get("interval") / 1000);
		long bandwidth = (Long) log.getParams().get("bandwidth");
		long rxBytesDelta = (Long) log.getParams().get("rx_bytes_delta");
		long txBytesDelta = (Long) log.getParams().get("tx_bytes_delta");
		long rxBps = rxBytesDelta * 8 / seconds;
		long txBps = txBytesDelta * 8 / seconds;
		int rxUsage = (int) (rxBps * 100 / bandwidth);
		int txUsage = (int) (txBps * 100 / bandwidth);

		int usage = Math.max(rxUsage, txUsage);
		return usage;
	}

	private String addUnit(long value, String postfix) {
		String level = "";
		if (value > 1000) {
			value = value / 1000;
			level = "K";

			if (value > 1000) {
				value = value / 1000;
				level = "M";

				if (value > 1000) {
					value = value / 1000;
					level = "G";
				}
			}
		}

		return value + level + postfix;
	}

	private NetworkInterface findNetworkInterface(String name) {
		try {
			Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
			while (nics.hasMoreElements()) {
				NetworkInterface ni = nics.nextElement();
				if (match(ni.getDisplayName(), name))
					return ni;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private boolean match(String lhs, String rhs) {
		lhs = lhs.replaceAll("[#()-/_\\[\\]]", " ");
		rhs = rhs.replaceAll("[#()-/_\\[\\]]", " ");
		String[] lTokens = lhs.split(" ");
		String[] rTokens = rhs.split(" ");

		if (lTokens.length != rTokens.length)
			return false;

		for (int i = 0; i < lTokens.length; i++)
			if (!lTokens[i].equals(rTokens[i]))
				return false;

		return true;
	}

	private Integer getType(String name) {
		NetworkInterface ni = findNetworkInterface(name);
		if (ni == null)
			return null;

		if (ni.getName().startsWith("eth"))
			return 6;

		return null;
	}

	private String getMacAddress(String name) {
		try {
			NetworkInterface ni = findNetworkInterface(name);
			if (ni == null)
				return null;

			byte[] b = ni.getHardwareAddress();
			if (b == null || b.length != 6)
				return null;

			return String.format("%02x:%02x:%02x:%02x:%02x:%02x", b[0], b[1], b[2], b[3], b[4], b[5]);
		} catch (SocketException e) {
		}
		return null;
	}

	private Integer getMtu(String name) {
		try {
			NetworkInterface ni = findNetworkInterface(name);
			if (ni == null)
				return 0;

			return ni.getMTU();
		} catch (SocketException e) {
		}
		return null;
	}

	private static boolean isFiltered(String name, String macAddress) {
		if (macAddress == null)
			return true;

		if (name.startsWith("isatap."))
			return true;

		if (name.equals("Built-in Infrared Device"))
			return true;

		return false;
	}

	private static class Counters {
		String instanceName;
		PerformanceCounter bandwidth;
		PerformanceCounter inBytesPerSec;
		PerformanceCounter outBytesPerSec;
		PerformanceCounter inUcastPktsPerSec;
		PerformanceCounter outUcastPktsPerSec;
		PerformanceCounter inNUcastPktsPerSec;
		PerformanceCounter outNUcastPktsPerSec;
		PerformanceCounter inErrors;
		PerformanceCounter outErrors;
		PerformanceCounter inDiscards;
		PerformanceCounter outDiscards;
	}

}

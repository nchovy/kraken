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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.Integer32;

/**
 * @author stania
 */
public class NetworkUsage {
	public static enum LogEntry {
		index(Interface.IfEntry.ifIndex), description(Interface.IfEntry.ifDescr), type(Interface.IfEntry.ifType), mtu(
				Interface.IfEntry.ifMtu), bandwidth(Interface.IfEntry.ifSpeed), mac(Interface.IfEntry.ifPhysAddress), admin_status(
				Interface.IfEntry.ifAdminStatus), oper_status(Interface.IfEntry.ifOperStatus), last_change(
				Interface.IfEntry.ifLastChange), rx_bytes_delta(Interface.IfEntry.ifInOctets), rx_ucast_pkts_delta(
				Interface.IfEntry.ifInUcastPkts), rx_nucast_pkts_delta(Interface.IfEntry.ifInNUcastPkts), rx_discards_delta(
				Interface.IfEntry.ifInDiscards), rx_errors_delta(Interface.IfEntry.ifInErrors), rx_unknown_protos(
				Interface.IfEntry.ifInUnknownProtos), tx_bytes_delta(Interface.IfEntry.ifOutOctets), tx_ucast_pkts_delta(
				Interface.IfEntry.ifOutUcastPkts), tx_nucast_pkts_delta(Interface.IfEntry.ifOutNUcastPkts), tx_discards_delta(
				Interface.IfEntry.ifOutDiscards), tx_errors_delta(Interface.IfEntry.ifOutErrors), tx_queue_length(
				Interface.IfEntry.ifOutQLen), specific(Interface.IfEntry.ifSpecific);

		@SuppressWarnings("unused")
		private Interface.IfEntry ifEntry;

		private LogEntry(Interface.IfEntry ifEntry) {
			this.ifEntry = ifEntry;
		}

		public static EnumSet<LogEntry> counters = EnumSet.of(LogEntry.rx_bytes_delta, LogEntry.rx_ucast_pkts_delta,
				LogEntry.rx_nucast_pkts_delta, LogEntry.rx_discards_delta, LogEntry.rx_errors_delta,
				LogEntry.rx_unknown_protos, LogEntry.tx_bytes_delta, LogEntry.tx_ucast_pkts_delta,
				LogEntry.tx_nucast_pkts_delta, LogEntry.tx_discards_delta, LogEntry.tx_errors_delta,
				LogEntry.tx_queue_length);
	}

	private Map<LogEntry, Object> properties = new EnumMap<LogEntry, Object>(LogEntry.class);
	private int ifIndex;
	private Logger kLogger = LoggerFactory.getLogger(this.getClass().getName());

	public int getIfIndex() {
		return ifIndex;
	}

	public void setIfIndex(int ifIndex) {
		this.ifIndex = ifIndex;
	}

	public NetworkUsage(int ifIndex) {
		this.ifIndex = ifIndex;
		properties.put(LogEntry.index, new Integer32(ifIndex));
	}

	public Object getProperty(LogEntry entry) {
		return properties.get(entry);
	}

	public void setProperty(LogEntry entry, Object value) {
		properties.put(entry, value);
	}

	public Map<LogEntry, Object> getProperties() {
		return properties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		NetworkUsage other = (NetworkUsage) obj;
		if (ifIndex != other.ifIndex)
			return false;
		return true;
	}

	public void save(File interfaceBasePath) {
		FileOutputStream fos = null;
		try {
			File file = new File(interfaceBasePath, "network-usage.obj");
			fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.write(ifIndex);
			for (Entry<LogEntry, Object> entry : properties.entrySet()) {
				oos.writeObject(entry.getKey());
				oos.writeObject(entry.getValue());
			}
		} catch (IOException ioe) {
			kLogger.warn("IOException while saving");
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (Exception e) {
			}
		}
	}
}

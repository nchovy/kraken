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
import org.snmp4j.smi.OID;

/**
 * @author stania
 */
public class Interface {
	public final static OID oidInterfaces = new OID(".1.3.6.1.2.1.2");
	public final static OID oidIfTable = new OID(".1.3.6.1.2.1.2.2");

	public static enum IfEntry {
		ifTable(oidIfTable.toString()),
		ifIndex(oidIfTable + ".1"),
		ifDescr(oidIfTable + ".2"),
		ifType(oidIfTable + ".3"),
		ifMtu(oidIfTable + ".4"),
		ifSpeed(oidIfTable + ".5"),
		ifPhysAddress(oidIfTable + ".6"),
		ifAdminStatus(oidIfTable + ".7"),
		ifOperStatus(oidIfTable + ".8"),
		ifLastChange(oidIfTable + ".9"),
		ifInOctets(oidIfTable + ".10"),
		ifInUcastPkts(oidIfTable + ".11"),
		ifInNUcastPkts(oidIfTable + ".12"),
		ifInDiscards(oidIfTable + ".13"),
		ifInErrors(oidIfTable + ".14"),
		ifInUnknownProtos(oidIfTable + ".15"),
		ifOutOctets(oidIfTable + ".16"),
		ifOutUcastPkts(oidIfTable + ".17"),
		ifOutNUcastPkts(oidIfTable + ".18"),
		ifOutDiscards(oidIfTable + ".19"),
		ifOutErrors(oidIfTable + ".20"),
		ifOutQLen(oidIfTable + ".21"),
		ifSpecific(oidIfTable + ".22"),
		INTERVAL("");

		private OID oid;

		private IfEntry(String oid) {
			this.oid = new OID(oid);
		}

		public OID getOID() {
			return this.oid;
		}

		public static IfEntry valueOf(OID oid) {
			if (oid.startsWith(oidIfTable)) {
				return IfEntry.values()[oid.last()];
			} else {
				return null;
			}
		}

		public static EnumSet<IfEntry> counters = EnumSet.of(
				IfEntry.ifInOctets,
				IfEntry.ifInUcastPkts,
				IfEntry.ifInNUcastPkts,
				IfEntry.ifInDiscards,
				IfEntry.ifInErrors,
				IfEntry.ifInUnknownProtos,
				IfEntry.ifOutOctets,
				IfEntry.ifOutUcastPkts,
				IfEntry.ifOutNUcastPkts,
				IfEntry.ifOutDiscards,
				IfEntry.ifOutErrors
				);
	}

	private Map<IfEntry, Object> properties = new EnumMap<IfEntry, Object>(IfEntry.class);
	private int ifIndex;
	private Logger kLogger = LoggerFactory.getLogger(this.getClass().getName());

	public int getIfIndex() {
		return ifIndex;
	}

	public void setIfIndex(int ifIndex) {
		this.ifIndex = ifIndex;
	}

	public Interface(int ifIndex) {
		this.ifIndex = ifIndex;
		properties.put(IfEntry.ifIndex, new Integer32(ifIndex));
	}

	public Object getProperty(IfEntry entry) {
		return properties.get(entry);
	}

	public void setProperty(IfEntry entry, Object value) {
		properties.put(entry, value);
	}

	public Map<IfEntry, Object> getProperties() {
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
		Interface other = (Interface) obj;
		if (ifIndex != other.ifIndex)
			return false;
		return true;
	}

	public void save(File interfaceBasePath) {
		FileOutputStream fos = null;
		try {
			File file = new File(interfaceBasePath, "iface.obj");
			fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.write(ifIndex);
			for (Entry<IfEntry, Object> entry : properties.entrySet()) {
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

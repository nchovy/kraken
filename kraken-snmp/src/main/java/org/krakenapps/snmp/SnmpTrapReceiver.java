package org.krakenapps.snmp;

public interface SnmpTrapReceiver {
	void handle(SnmpTrap trap);
}

package org.krakenapps.snmpmon;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerSpecification;
import org.krakenapps.log.api.SimpleLog;
import org.krakenapps.snmp.SnmpTrap;

public class SnmpTrapLogger extends AbstractLogger {

	public SnmpTrapLogger(LoggerSpecification spec, LoggerFactory loggerFactory) {
		super(spec.getNamespace(), spec.getName(), spec.getDescription(), loggerFactory, spec.getConfig());
	}

	@Override
	protected void runOnce() {
	}

	public void push(SnmpTrap trap) {
		Map<String, Object> m = new HashMap<String, Object>(trap.getVariableBindings());
		m.put("generic_trap", toGenericTrapString(trap.getGenericTrap()));
		write(new SimpleLog(new Date(), getFullName(), m));
	}

	private String toGenericTrapString(int type) {
		switch (type) {
		case 0:
			return "coldStart";
		case 1:
			return "warmStart";
		case 2:
			return "linkDown";
		case 3:
			return "linkUp";
		case 4:
			return "authenticationFailure";
		case 5:
			return "egpNeighborLoss";
		case 6:
			return "enterpriseSpecific";
		default:
			return "unknown: " + type;
		}
	}
}

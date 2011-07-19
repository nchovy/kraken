package org.krakenapps.snmp;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class SnmpTrap {
	private InetSocketAddress remoteAddress;
	private InetSocketAddress localAddress;
	private int version;
	private int genericTrap;
	private int specificTrap;
	private Map<String, Object> variableBindings = new HashMap<String, Object>();

	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(InetSocketAddress localAddress) {
		this.localAddress = localAddress;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getGenericTrap() {
		return genericTrap;
	}

	public void setGenericTrap(int genericTrap) {
		this.genericTrap = genericTrap;
	}

	public int getSpecificTrap() {
		return specificTrap;
	}

	public void setSpecificTrap(int specificTrap) {
		this.specificTrap = specificTrap;
	}

	public Map<String, Object> getVariableBindings() {
		return variableBindings;
	}

	public void setVariableBindings(Map<String, Object> variableBindings) {
		this.variableBindings = variableBindings;
	}
}

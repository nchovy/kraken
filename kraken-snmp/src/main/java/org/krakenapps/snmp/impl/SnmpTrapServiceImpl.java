package org.krakenapps.snmp.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.snmp.SnmpTrapBinding;
import org.krakenapps.snmp.SnmpTrapReceiver;
import org.krakenapps.snmp.SnmpTrapService;

@Component(name = "snmp-trap-service")
@Provides
public class SnmpTrapServiceImpl implements SnmpTrapService {

	private Map<String, SnmpTrapBinding> bindings;

	private CopyOnWriteArraySet<SnmpTrapReceiver> callbacks;

	public SnmpTrapServiceImpl() {
		bindings = new ConcurrentHashMap<String, SnmpTrapBinding>();
		callbacks = new CopyOnWriteArraySet<SnmpTrapReceiver>();
	}

	@Validate
	public void start() {
	}

	@Invalidate
	public void stop() {
		bindings.clear();
		callbacks.clear();
	}

	@Override
	public List<String> getBindingNames() {
		return new ArrayList<String>(bindings.keySet());
	}

	@Override
	public SnmpTrapBinding getBinding(String name) {
		return bindings.get(name);
	}

	@Override
	public void open(String name, Map<String, String> props) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addReceiver(SnmpTrapReceiver callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeReceiver(SnmpTrapReceiver callback) {
		callbacks.remove(callback);
	}

}

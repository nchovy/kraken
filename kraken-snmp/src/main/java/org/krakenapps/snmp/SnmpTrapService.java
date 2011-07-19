package org.krakenapps.snmp;

import java.util.List;
import java.util.Map;

public interface SnmpTrapService {
	List<String> getBindingNames();

	SnmpTrapBinding getBinding(String name);

	void open(String name, Map<String, String> props);

	void close(String name);

	void addReceiver(SnmpTrapReceiver callback);

	void removeReceiver(SnmpTrapReceiver callback);
}

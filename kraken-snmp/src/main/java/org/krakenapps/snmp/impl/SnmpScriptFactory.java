package org.krakenapps.snmp.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.snmp.SnmpScript;

@Component(name = "snmp-script-factory")
@Provides
public class SnmpScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "snmp")
	private String alias;

	@Override
	public Script createScript() {
		return new SnmpScript();
	}

}

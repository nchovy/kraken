package org.krakenapps.portmon.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.portmon.PortMonitor;

@Component(name = "port-monitor-script-factory")
@Provides
public class PortMonitorScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "portmon")
	private String alias;

	@Requires
	private PortMonitor monitor;

	@Override
	public Script createScript() {
		return new PortMonitorScript(monitor);
	}

}

package org.krakenapps.sleepproxy.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.osgi.framework.BundleContext;

@Component(name = "sleep-proxy-script-factory")
@Provides
public class SleepProxyScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "sleepproxy")
	private String alias;
	
	private BundleContext bc;

	public SleepProxyScriptFactory(BundleContext bc) {
		this.bc = bc;
	}

	@Override
	public Script createScript() {
		return new SleepProxyScript(bc);
	}
}

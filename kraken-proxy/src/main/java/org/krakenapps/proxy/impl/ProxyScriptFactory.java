package org.krakenapps.proxy.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.proxy.ForwardProxy;

@Component(name = "proxy-script-factory")
@Provides
public class ProxyScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "proxy")
	private String alias;

	@Requires
	private ForwardProxy forwardProxy;
	
	@Override
	public Script createScript() {
		return new ProxyScript(forwardProxy);
	}
}

package org.krakenapps.servlet.xmlrpc.internal;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.servlet.xmlrpc.XmlRpcMethodRegistry;

@Component(name = "kraken-xmlrpc-servlet-script-factory")
@Provides
public class XmlRpcServletScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "xmlrpc-servlet")
	private String alias;

	@Requires
	private XmlRpcMethodRegistry registry;

	@Override
	public Script createScript() {
		return new XmlRpcServletScript(registry);
	}

}

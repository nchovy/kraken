package org.krakenapps.dns.script;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.dns.DnsService;
import org.krakenapps.dns.ProxyResolverProvider;

@Component(name = "dns-script-factory")
@Provides
public class DnsScriptFactory implements ScriptFactory {

	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "dns")
	private String alias;

	@Requires
	private DnsService dns;

	@Requires
	private ProxyResolverProvider proxy;

	@Override
	public Script createScript() {
		return new DnsScript(dns, proxy);
	}
}

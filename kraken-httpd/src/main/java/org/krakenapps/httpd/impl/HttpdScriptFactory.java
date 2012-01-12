package org.krakenapps.httpd.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.httpd.HttpService;

@Component(name = "httpd-script-factory")
@Provides
public class HttpdScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "httpd")
	private String alias;

	@Requires
	private HttpService httpd;

	@Override
	public Script createScript() {
		return new HttpdScript(httpd);
	}

}

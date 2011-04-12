package org.krakenapps.querybrowser.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.querybrowser.ConnectionStringRegistry;

@Component(name = "querybrowser-script-factory")
@Provides
public class QueryBrowserScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "qb")
	private String alias;

	@Requires
	private ConnectionStringRegistry registry;

	@Override
	public Script createScript() {
		return new QueryBrowserScript(registry);
	}

}

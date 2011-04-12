package org.krakenapps.datasource.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.datasource.DataConverterRegistry;
import org.krakenapps.datasource.DataSourceRegistry;

@Component(name = "data-source-script-factory")
@Provides
public class DataSourceScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "datasource")
	private String alias;

	@Requires
	private DataSourceRegistry sourceRegistry;

	@Requires
	private DataConverterRegistry converterRegistry;

	@Override
	public Script createScript() {
		return new DataSourceScript(sourceRegistry, converterRegistry);
	}

}

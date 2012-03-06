package org.krakenapps.servlet.csv.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.servlet.csv.CsvHttpServiceApi;
import org.krakenapps.servlet.csv.CsvHttpScript;

@Component(name = "csv-http-script-factory")
@Provides
public class CsvHttpScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "csv-servlet")
	private String alias;

	@Requires
	private CsvHttpServiceApi api;

	@Override
	public Script createScript() {
		return new CsvHttpScript(api);
	}
}

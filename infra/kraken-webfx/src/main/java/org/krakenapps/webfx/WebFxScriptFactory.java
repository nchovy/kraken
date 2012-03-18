package org.krakenapps.webfx;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;

@Component(name = "webfx-script-factory")
@Provides
public class WebFxScriptFactory implements ScriptFactory {

	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "webfx")
	private String alias;

	@Requires
	private WebApplicationRegistry appRegistry;

	@Override
	public Script createScript() {
		return new WebFxScript(appRegistry);
	}

}

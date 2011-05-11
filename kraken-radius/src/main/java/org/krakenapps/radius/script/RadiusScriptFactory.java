package org.krakenapps.radius.script;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;

@Component(name = "radius-script-factory")
@Provides
public class RadiusScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "radius")
	private String alias;

	@Override
	public Script createScript() {
		return new RadiusScript();
	}

}

package org.krakenapps.linux.api.script;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;

@Component(name = "linux-api-script-factory")
@Provides
public class LinuxApiScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "linux")
	private String alias;

	@Override
	public Script createScript() {
		return new LinuxApiScript();
	}

}

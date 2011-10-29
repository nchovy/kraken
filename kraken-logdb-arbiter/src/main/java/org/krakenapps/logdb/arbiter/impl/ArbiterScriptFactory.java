package org.krakenapps.logdb.arbiter.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.logdb.arbiter.ArbiterService;

@Component(name = "logdb-arbiter-script-factory")
@Provides
public class ArbiterScriptFactory implements ScriptFactory {

	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "arbiter")
	private String alias;
	
	@Requires
	private ArbiterService arbiter;

	@Override
	public Script createScript() {
		return new ArbiterScript(arbiter);
	}

}

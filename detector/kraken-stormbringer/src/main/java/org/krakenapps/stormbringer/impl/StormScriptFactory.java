package org.krakenapps.stormbringer.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.stormbringer.ArpPoisoner;

@Component(name = "storm-script-factory")
@Provides
public class StormScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "storm")
	private String alias;

	@Requires
	private ArpPoisoner arpPoisoner;

	@Override
	public Script createScript() {
		return new StormScript(arpPoisoner);
	}
}

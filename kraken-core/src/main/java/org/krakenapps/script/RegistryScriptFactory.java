package org.krakenapps.script;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.main.Kraken;

public class RegistryScriptFactory implements ScriptFactory {
	@Override
	public Script createScript() {
		return new RegistryScript(Kraken.getContext());
	}
}

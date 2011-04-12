package org.krakenapps.script;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;

public class PerfScriptFactory implements ScriptFactory {

	@Override
	public Script createScript() {
		return new PerfScript();
	}

}

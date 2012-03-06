package org.krakenapps.thread;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;

public class ThreadScriptFactory implements ScriptFactory {

	@Override
	public Script createScript() {
		return new ThreadScript();
	}

}

package org.krakenapps.dns.impl;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.dns.DnsScript;

public class DnsScriptFactory implements ScriptFactory {

	@Override
	public Script createScript() {
		return new DnsScript();
	}

}

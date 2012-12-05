package org.krakenapps.syslogmon.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.syslogmon.SyslogClassifierRegistry;

@Component(name = "syslogmon-script-factory")
@Provides
public class SyslogmonScriptFactory implements ScriptFactory {
	@Requires
	private SyslogClassifierRegistry classifierRegistry;

	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "syslogmon")
	private String alias;

	@Override
	public Script createScript() {
		return new SyslogmonScript(classifierRegistry);
	}
}

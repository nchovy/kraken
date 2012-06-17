package org.krakenapps.msgbus.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.ResourceApi;

@Component(name = "msgbus-script-factory")
@Provides
public class MsgbusScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "msgbus")
	private String alias;

	@Requires
	private MessageBus msgbus;
	
	@Requires
	private ResourceApi resourceApi;

	@Override
	public Script createScript() {
		return new MsgbusScript(msgbus, resourceApi);
	}

}

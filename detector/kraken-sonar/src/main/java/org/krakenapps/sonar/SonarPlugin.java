package org.krakenapps.sonar;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "sonar-plugin")
@MsgbusPlugin
public class SonarPlugin {
	@Requires
	private SonarApi sonarApi;
}

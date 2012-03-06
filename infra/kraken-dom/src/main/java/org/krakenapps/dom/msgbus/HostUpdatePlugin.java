package org.krakenapps.dom.msgbus;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.api.HostUpdateApi;
import org.krakenapps.dom.model.Host;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.AllowGuestAccess;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@MsgbusPlugin
@Component(name = "dom-host-update-plugin")
public class HostUpdatePlugin {
	@Requires
	private ConfigManager conf;

	@Requires
	private HostApi hostApi;

	@Requires
	private HostUpdateApi hostUpdate;

	@MsgbusMethod
	@AllowGuestAccess
	public void update(Request req, Response resp) {
		Host oldHost = hostApi.findHost(req.getOrgDomain(), req.getString("guid"));
		if (oldHost == null)
			oldHost = new Host();

		Host host = (Host) PrimitiveConverter.overwrite(oldHost, req.getParams(), conf.getParseCallback(req.getOrgDomain()));
		hostUpdate.update(host);
	}
}

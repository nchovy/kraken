package org.krakenapps.dom.msgbus;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.api.HostUpdateApi;
import org.krakenapps.dom.model.Host;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.AllowGuestAccess;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MsgbusPlugin
@Component(name = "dom-host-update-plugin")
public class HostUpdatePlugin {
	private final Logger logger = LoggerFactory.getLogger(HostUpdatePlugin.class.getName());
	@Requires
	private ConfigManager conf;

	@Requires
	private HostApi hostApi;

	@Requires
	private HostUpdateApi hostUpdate;

	@MsgbusMethod
	@AllowGuestAccess
	public void update(Request req, Response resp) {
		
		ConfigDatabase db = conf.findDatabase(req.getString("org_domain"));
		
		if ( db == null ){
			logger.error("kraken-dom: invalid domain name");
			return;
		}
		
		Host oldHost = hostApi.findHost(req.getString("org_domain"), req.getString("guid"));
		if (oldHost == null)
			oldHost = new Host();

		Host host = (Host) PrimitiveConverter.overwrite(oldHost, req.getParams(), conf.getParseCallback(req.getString("org_domain")));
		logger.trace("kraken dom: host guid [{}] is update", host.getGuid());
		hostUpdate.update(host);
	}
}

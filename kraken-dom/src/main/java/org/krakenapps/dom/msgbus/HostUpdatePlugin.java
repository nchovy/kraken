package org.krakenapps.dom.msgbus;

import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.api.PrimitiveParseCallback;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.api.HostUpdateApi;
import org.krakenapps.dom.model.Host;
import org.krakenapps.dom.model.HostType;
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
	@Requires
	private HostUpdateApi hostUpdate;
	@Requires
	private HostApi hostApi;
	@Requires
	private ConfigManager conf;

	private final Logger logger = LoggerFactory.getLogger(HostUpdatePlugin.class.getName());

	@MsgbusMethod
	@AllowGuestAccess
	public void update(Request req, Response resp) {

		Host oldHost = hostApi.findHost("localhost", req.getString("guid"));

		if (oldHost == null)
			oldHost = new Host();

		Host host = (Host) PrimitiveConverter.overwrite(oldHost, req.getParams(), new PrimitiveParseCallback() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> T onParse(Class<T> clazz, Map<String, Object> referenceKey) {
				return (T) PrimitiveConverter.overwrite(new HostType(), referenceKey);
			}
		});

		hostUpdate.update(host);
	}
}

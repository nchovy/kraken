package org.krakenapps.msgbus;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.Session;
import org.krakenapps.msgbus.handler.CallbackType;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "msgbus-push-plugin")
@MsgbusPlugin
public class PushPlugin {
	@Requires
	private PushApi pushApi;

	@MsgbusMethod
	public void subscribe(Request req, Response resp) {
		int processId = Integer.parseInt(req.getSource());
		String method = req.getString("callback");
		pushApi.subscribe(req.getOrgId(), req.getSession().getId(), processId, method);
	}

	@MsgbusMethod
	public void unsubscribe(Request req, Response resp) {
		int processId = Integer.parseInt(req.getSource());
		String method = req.getString("callback");
		pushApi.unsubscribe(req.getOrgId(), req.getSession().getId(), processId, method);
	}

	@MsgbusMethod(type = CallbackType.SessionClosed)
	public void sessionClosed(Session session) {
		if (pushApi != null && session != null && session.getOrgId() != null)
			pushApi.sessionClosed(session.getOrgId(), session.getId());
	}
}
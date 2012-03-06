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
		String orgDomain = getOrgDomain(req.getSession());

		pushApi.subscribe(orgDomain, req.getSession().getId(), processId, method);
	}

	@MsgbusMethod
	public void unsubscribe(Request req, Response resp) {
		int processId = Integer.parseInt(req.getSource());
		String method = req.getString("callback");
		String orgDomain = getOrgDomain(req.getSession());

		pushApi.unsubscribe(orgDomain, req.getSession().getId(), processId, method);
	}

	@MsgbusMethod(type = CallbackType.SessionClosed)
	public void sessionClosed(Session session) {
		String orgDomain = getOrgDomain(session);

		if (pushApi != null && session != null && orgDomain != null)
			pushApi.sessionClosed(orgDomain, session.getId());
	}

	@SuppressWarnings("deprecation")
	private String getOrgDomain(Session session) {
		String orgDomain = session.getOrgDomain();

		// for backward compatibility
		Integer orgId = session.getOrgId();
		if (orgDomain == null && orgId != null)
			orgDomain = orgId.toString();

		return orgDomain;
	}
}
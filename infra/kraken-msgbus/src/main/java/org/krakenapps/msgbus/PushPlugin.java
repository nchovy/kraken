/*
 * Copyright 2011 Future Systems, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.msgbus;

import java.util.HashMap;

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

		pushApi.subscribe(orgDomain, req.getSession().getGuid(), processId, method, new HashMap<String, Object>());
	}

	@MsgbusMethod
	public void unsubscribe(Request req, Response resp) {
		int processId = Integer.parseInt(req.getSource());
		String method = req.getString("callback");
		String orgDomain = getOrgDomain(req.getSession());

		pushApi.unsubscribe(orgDomain, req.getSession().getGuid(), processId, method);
	}

	@MsgbusMethod(type = CallbackType.SessionClosed)
	public void sessionClosed(Session session) {
		String orgDomain = getOrgDomain(session);

		if (pushApi != null && session != null && orgDomain != null)
			pushApi.sessionClosed(orgDomain, session.getGuid());
	}

	private String getOrgDomain(Session session) {
		return session.getOrgDomain();
	}
}
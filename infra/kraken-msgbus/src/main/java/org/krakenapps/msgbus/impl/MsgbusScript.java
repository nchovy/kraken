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
package org.krakenapps.msgbus.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.api.Primitive;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.msgbus.AbstractSession;
import org.krakenapps.msgbus.Message;
import org.krakenapps.msgbus.Message.Type;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.ResourceApi;
import org.krakenapps.msgbus.ResourceHandler;
import org.krakenapps.msgbus.Session;

public class MsgbusScript implements Script {
	private MessageBus msgbus;
	private ResourceApi resourceApi;
	private ScriptContext context;

	public MsgbusScript(MessageBus msgbus, ResourceApi resourceApi) {
		this.msgbus = msgbus;
		this.resourceApi = resourceApi;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void plugins(String[] args) {
		String filter = null;
		if (args.length > 0)
			filter = args[0];

		context.println("Msgbus Plugins");
		context.println("-------------------");
		for (String name : msgbus.getPluginNames()) {
			if (filter != null && !name.contains(filter))
				continue;

			context.println(name);

			for (String method : msgbus.getMethodNames(name)) {
				context.println("    " + method.substring(name.length() + 1));
			}

			context.println("");
		}

	}

	public void sessions(String[] args) {
		context.println("Msgbus Sessions");
		context.println("-------------------");
		for (Session session : msgbus.getSessions()) {
			context.println(session.toString());
		}
	}

	@ScriptUsage(description = "run msgbus method", arguments = {
			@ScriptArgument(name = "org domain", type = "string", description = "org domain"),
			@ScriptArgument(name = "admin login name", type = "string", description = "admin login name"),
			@ScriptArgument(name = "method", type = "string", description = "package.method format"),
			@ScriptArgument(name = "arguments", type = "string", description = "key=value format arguments", optional = true) })
	public void run(String[] args) throws InterruptedException {
		String orgDomain = args[0];
		String adminLoginName = args[1];
		String method = args[2];

		Map<String, Object> m = new HashMap<String, Object>();
		for (int i = 3; i < args.length; i++) {
			String arg = args[i];
			int pos = arg.indexOf('=');
			String s = arg;
			String value = null;
			if (pos >= 0) {
				s = arg.substring(0, pos);
				value = arg.substring(pos + 1);
			}

			m.put(s, value);
		}

		Message msg = new Message();
		msg.setType(Type.Request);
		msg.setMethod(method);
		msg.setParameters(m);

		Date begin = new Date();
		ScriptSession s = new ScriptSession(orgDomain, adminLoginName);
		msgbus.dispatch(s, msg);
		while (true) {
			if (s.completed)
				break;

			Date now = new Date();
			if (now.getTime() - begin.getTime() > 5000)
				break;

			Thread.sleep(100);
		}
	}

	@ScriptUsage(description = "send", arguments = {
			@ScriptArgument(name = "session guid", type = "string", description = "session guid"),
			@ScriptArgument(name = "callback name", type = "string", description = "callback name"),
			@ScriptArgument(name = "data string", type = "string", description = "data string") })
	public void send(String[] args) {
		Session session = msgbus.getSession(args[0]);
		if (session == null) {
			context.println("session not found");
			return;
		}

		// trap without push api (test without push subscription)
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("data", args[2]);

		Message msg = new Message();
		msg.setType(Type.Trap);
		msg.setSession(session.getGuid());
		msg.setMethod(args[1]);
		msg.setParameters(params);
		session.send(msg);
		context.println("sent trap message");
	}

	public void resourceHandlers(String[] args) {
		context.println("Resource Handlers");
		context.println("-------------------");
		for (String key : resourceApi.getResourceHandlerKeys()) {
			ResourceHandler handler = resourceApi.getResourceHandler(key);
			context.println("[" + key + "] " + handler);
		}
	}

	private class ScriptSession extends AbstractSession {
		private boolean completed;
		private String orgDomain;
		private String adminLoginName;

		public ScriptSession(String orgDomain, String adminLoginName) {
			this.orgDomain = orgDomain;
			this.adminLoginName = adminLoginName;
		}

		@Override
		public String getOrgDomain() {
			return orgDomain;
		}

		@Override
		public String getAdminLoginName() {
			return adminLoginName;
		}

		@Override
		public void send(Message msg) {
			if (msg.getErrorCode() != null)
				context.println(msg.getErrorCode() + ": " + msg.getErrorMessage());
			else
				context.println(Primitive.stringify(msg.getParameters()));

			completed = true;
		}
	}

	@ScriptUsage(description = "killSession", arguments = { @ScriptArgument(name = "session guid", type = "string", description = "session guid") })
	public void killSession(String[] args) {
		Session session = msgbus.getSession(args[0]);
		if (session == null) {
			context.println("session not found");
			return;
		}
		msgbus.closeSession(session);
		context.println("[" + session.getGuid() + "] closed");

	}

	@ScriptUsage(description = "set msgbus session timeout", arguments = { @ScriptArgument(name = "sesstion timeout", type = "string", description = "minute") })
	public void setTimeout(String[] args) {

		msgbus.setSessionTimeout(Integer.parseInt(args[0]));
		context.println("set");
		context.println("-------------------");
	}

	public void getTimeout(String[] args) {		
		context.println("timeout=" + msgbus.getSessionTimeout());
	}
}

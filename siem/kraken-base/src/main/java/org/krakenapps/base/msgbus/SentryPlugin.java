/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.base.msgbus;

import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.base.SentryProxy;
import org.krakenapps.base.SentryProxyRegistry;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "sentry-plugin")
@MsgbusPlugin
public class SentryPlugin {
	@Requires
	private SentryProxyRegistry registry;

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void callSentry(Request req, Response resp) {
		String hostGuid = req.getString("guid");

		SentryProxy s = registry.getSentry(hostGuid);
		if (s == null)
			throw new MsgbusException("kraken-base", "sentry-not-connected");

		String method = req.getString("method");
		List<Object> args = (List<Object>) req.get("args");

		try {
			Object ret = s.call(method, args.toArray());
			resp.put("result", ret);
		} catch (Exception e) {
			throw new MsgbusException("kraken-base", "sentry-rpc-error");
		}
	}
}

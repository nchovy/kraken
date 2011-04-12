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
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.exception.SentryNotConnectedException;
import org.krakenapps.dom.exception.SentryNotFoundException;
import org.krakenapps.dom.exception.SentryRpcException;
import org.krakenapps.dom.model.Sentry;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "sentry-plugin")
@MsgbusPlugin
public class SentryPlugin {
	@Requires
	private HostApi hostApi;
	@Requires
	private SentryApi sentryApi;
	@Requires
	private SentryProxyRegistry registry;

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void callSentry(Request req, Response resp) {
		Sentry sentry = sentryApi.getSentry(req.getOrgId(), req.getInteger("host_id"));
		if (sentry == null)
			throw new SentryNotFoundException();

		SentryProxy s = registry.getSentry(sentry.getHost().getGuid());
		if (s == null)
			throw new SentryNotConnectedException();

		String method = req.getString("method");
		List<Object> args = (List<Object>) req.get("args");

		try {
			Object ret = s.call(method, args.toArray());
			resp.put("result", ret);
		} catch (Exception e) {
			throw new SentryRpcException();
		}
	}

	@MsgbusMethod
	public void getSentries(Request req, Response resp) {
		List<Sentry> sentries = sentryApi.getSentries(req.getOrgId());
		resp.put("sentries", Marshaler.marshal(sentries));
	}

	@MsgbusMethod
	public void getSentry(Request req, Response resp) {
		int hostId = req.getInteger("id");
		Sentry sentry = sentryApi.getSentry(req.getOrgId(), hostId);
		resp.put("sentry", sentry);
	}

	@MsgbusMethod
	public void createSentry(Request req, Response resp) {
		if (req.has("host_id")) {
			int hostId = req.getInteger("host_id");
			int sentryId = sentryApi.createSentry(req.getOrgId(), hostId);
			resp.put("id", sentryId);
		} else {
			int hostTypeId = req.getInteger("host_type_id");
			int areaId = req.getInteger("area_id");
			String hostName = req.getString("name");
			String description = req.getString("description");

			int hostId = hostApi.createHost(req.getOrgId(), hostTypeId, areaId, hostName, description);
			int sentryId = sentryApi.createSentry(req.getOrgId(), hostId);
			resp.put("id", sentryId);
		}
	}

	@MsgbusMethod
	public void updateSentry(Request req, Response resp) {
		int hostId = req.getInteger("id");
		boolean isConnected = req.getBoolean("is_connected");
		sentryApi.updateSentry(req.getOrgId(), hostId, isConnected);
	}

	@MsgbusMethod
	public void removeSentry(Request req, Response resp) {
		int hostId = req.getInteger("id");
		sentryApi.removeSentry(req.getOrgId(), hostId);
	}

	@MsgbusMethod
	public void disconnectSentry(Request req, Response resp) {
		int hostId = req.getInteger("id");
		sentryApi.disconnectSentry(req.getOrgId(), hostId);
	}
}

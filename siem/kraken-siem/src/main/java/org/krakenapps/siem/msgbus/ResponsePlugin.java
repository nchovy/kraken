/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.siem.msgbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.krakenapps.siem.engine.EventResponseMapper;
import org.krakenapps.siem.engine.ResponseKey;
import org.krakenapps.siem.response.ResponseAction;
import org.krakenapps.siem.response.ResponseActionManager;
import org.krakenapps.siem.response.ResponseConfigOption;
import org.krakenapps.siem.response.ResponseServer;

@Component(name = "siem-response-plugin")
@MsgbusPlugin
public class ResponsePlugin {
	@Requires
	private ResponseServer respServer;

	@Requires
	private EventResponseMapper mapper;

	@MsgbusMethod
	public void getResponseManagers(Request req, Response resp) {
		List<String> managers = new ArrayList<String>();
		for (ResponseActionManager manager : respServer.getResponseActionManagers())
			managers.add(manager.getName());
		resp.put("managers", managers);
	}

	@MsgbusMethod
	public void getResponseActions(Request req, Response resp) {
		if (req.has("manager")) {
			String managerName = req.getString("manager");

			ResponseActionManager manager = respServer.getResponseActionManager(managerName);
			resp.put("actions", Marshaler.marshal(manager.getActions()));
		} else {
			List<Object> actions = new ArrayList<Object>();
			for (ResponseActionManager manager : respServer.getResponseActionManagers())
				actions.addAll(Marshaler.marshal(manager.getActions()));
			resp.put("actions", actions);
		}
	}

	@MsgbusMethod
	public void getResponseActionOptions(Request req, Response resp) {
		String managerName = req.getString("manager");
		Locale locale = new Locale(req.getString("locale"));
		ResponseActionManager manager = respServer.getResponseActionManager(managerName);
		resp.put("options", Marshaler.marshal(manager.getConfigOptions(), locale));
	}

	@MsgbusMethod
	public void createResponseAction(Request req, Response resp) {
		String managerName = req.getString("manager");
		String namespace = req.getString("namespace");
		String name = req.getString("name");

		ResponseActionManager manager = respServer.getResponseActionManager(managerName);

		Properties config = new Properties();
		for (ResponseConfigOption option : manager.getConfigOptions()) {
			String value = req.getString(option.getName());

			if (!value.isEmpty())
				config.put(option.getName(), value);
			else if (option.isRequired()) {
				return;
			}
		}

		manager.newAction(namespace, name, null, config);
	}

	@MsgbusMethod
	public void removeResponseAction(Request req, Response resp) {
		String managerName = req.getString("manager");
		String namespace = req.getString("namespace");
		String name = req.getString("name");

		ResponseActionManager manager = respServer.getResponseActionManager(managerName);

		manager.deleteAction(namespace, name);
	}

	@MsgbusMethod
	public void getResponseMappings(Request req, Response resp) {
		List<Object> mappings = new ArrayList<Object>();

		for (ResponseKey key : mapper.getKeys()) {
			Map<Object, Object> mapping = new HashMap<Object, Object>();
			mapping.put("category", key.getCategory());
			mapping.put("event_source", key.getEventSource());
			mapping.put("actions", Marshaler.marshal(mapper.getActions(key)));
			mappings.add(mapping);
		}

		resp.put("mappings", mappings);
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void createResponseMappings(Request req, Response resp) {
		String category = req.getString("category");
		List<Map<String, String>> params = (List<Map<String, String>>) req.get("actions");
		for (Map<String, String> param : params) {
			String managerName = param.get("manager");
			String namespace = param.get("namespace");
			String name = param.get("name");

			ResponseActionManager manager = respServer.getResponseActionManager(managerName);
			if (manager == null)
				continue;
			ResponseAction action = manager.getAction(namespace, name);
			if (action == null)
				continue;

			mapper.addResponse(new ResponseKey(category), action);
		}
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void removeResponseMappings(Request req, Response resp) {
		String category = req.getString("category");
		List<Map<String, String>> params = (List<Map<String, String>>) req.get("actions");
		for (Map<String, String> param : params) {
			String managerName = param.get("manager");
			String namespace = param.get("namespace");
			String name = param.get("name");

			ResponseActionManager manager = respServer.getResponseActionManager(managerName);
			if (manager == null)
				continue;
			ResponseAction action = manager.getAction(namespace, name);
			if (action == null)
				continue;

			mapper.removeResponse(new ResponseKey(category), action);
		}
	}
}

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
package org.krakenapps.dom.msgbus;

import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.WidgetApi;
import org.krakenapps.dom.exception.WidgetNotFoundException;
import org.krakenapps.dom.model.Widget;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-widget-plugin")
@MsgbusPlugin
public class WidgetPlugin {
	@Requires
	private WidgetApi widgetApi;

	@MsgbusMethod
	public void getWidgets(Request req, Response resp) {
		resp.put("widgets", fetchWidgets(req.getAdminId()));
	}

	private List<Object> fetchWidgets(int adminId) {
		return Marshaler.marshal(widgetApi.getWidgets(adminId));
	}

	@MsgbusMethod
	public void createWidget(Request req, Response resp) {
		int programId = req.getInteger("programId");
		Widget widget = widgetApi.createWidget(req.getOrgId(), req.getAdminId(), programId);
		resp.put("id", widget.getId());
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void updateWidget(Request req, Response resp) {
		int widgetId = req.getInteger("id");
		updateWidgetConfigs(req.getAdminId(), widgetId, (List<Object>) req.get("configs"));
	}

	@SuppressWarnings("unchecked")
	private void updateWidgetConfigs(int adminId, int widgetId, List<Object> configs) throws WidgetNotFoundException {
		for (Object o : configs) {
			Map<String, Object> config = (Map<String, Object>) o;
			String name = (String) config.get("name");
			String value = (String) config.get("value");
			widgetApi.setConfig(adminId, widgetId, name, value);
		}
	}

	@MsgbusMethod
	public void removeWidget(Request req, Response resp) {
		int widgetId = req.getInteger("widgetId");
		widgetApi.removeWidget(req.getAdminId(), widgetId);
	}

}
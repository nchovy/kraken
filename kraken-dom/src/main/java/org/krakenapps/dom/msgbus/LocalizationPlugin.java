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
import java.util.Locale;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.LocalizationApi;
import org.krakenapps.dom.api.ResourceKey;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-localization-plugin")
@MsgbusPlugin
public class LocalizationPlugin {
	@Requires
	private LocalizationApi localizationApi;

	@MsgbusMethod
	public void getTemplate(Request req, Response resp) {
		String group = req.getString("group");
		String key = req.getString("key");
		Locale locale = req.getSession().getLocale();

		String template = localizationApi.get(new ResourceKey(group, key, locale));
		resp.put("template", template);
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void format(Request req, Response resp) {
		String group = req.getString("group");
		String key = req.getString("key");
		Locale locale = req.getSession().getLocale();
		List<Object> args = (List<Object>) req.get("args");

		ResourceKey resourceKey = new ResourceKey(group, key, locale);
		String text = localizationApi.format(resourceKey, args.toArray());
		resp.put("text", text);
	}
}

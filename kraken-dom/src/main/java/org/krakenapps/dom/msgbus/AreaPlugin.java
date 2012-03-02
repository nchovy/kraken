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

import java.util.Collection;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.dom.api.AreaApi;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.model.Area;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-area-plugin")
@MsgbusPlugin
public class AreaPlugin {
	@Requires
	private ConfigManager conf;

	@Requires
	private AreaApi areaApi;

	@MsgbusMethod
	public void getRootAreas(Request req, Response resp) {
		Collection<Area> roots = areaApi.getRootAreas(req.getOrgDomain());
		resp.put("areas", PrimitiveConverter.serialize(roots, PrimitiveConverter.SerializeOption.INCLUDE_SKIP_FIELD));
	}

	@MsgbusMethod
	public void getArea(Request req, Response resp) {
		String guid = req.getString("guid");
		Area area = areaApi.getArea(req.getOrgDomain(), guid);
		resp.put("area", PrimitiveConverter.serialize(area, PrimitiveConverter.SerializeOption.INCLUDE_SKIP_FIELD));
	}

	@MsgbusMethod
	public void createArea(Request req, Response resp) {
		Area area = (Area) PrimitiveConverter.overwrite(new Area(), req.getParams());
		areaApi.createArea(req.getOrgDomain(), area);
		resp.put("guid", area.getGuid());
	}

	@MsgbusMethod
	public void updateArea(Request req, Response resp) {
		Area before = areaApi.getArea(req.getOrgDomain(), req.getString("guid"));
		Area area = (Area) PrimitiveConverter.overwrite(before, req.getParams(), conf.getParseCallback(req.getOrgDomain()));
		areaApi.updateArea(req.getOrgDomain(), area);
	}

	@MsgbusMethod
	public void removeArea(Request req, Response resp) {
		String guid = req.getString("guid");
		areaApi.removeArea(req.getOrgDomain(), guid);
	}

	@MsgbusMethod
	public void removeAreas(Request req, Response resp) {
		@SuppressWarnings("unchecked")
		Collection<String> guids = (Collection<String>) req.get("guids");
		areaApi.removeAreas(req.getOrgDomain(), guids);
	}
}

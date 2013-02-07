/*
 * Copyright 2011 Future Systems
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
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.MapApi;
import org.krakenapps.dom.model.MapInfo;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-map-plugin")
@MsgbusPlugin
public class MapPlugin {
	@Requires
	private ConfigManager conf;

	@Requires
	private MapApi mapApi;

	@MsgbusMethod
	public void getMaps(Request req, Response resp) {
		Collection<MapInfo> maps = mapApi.getMaps(req.getOrgDomain());
		resp.put("maps", PrimitiveConverter.serialize(maps));
	}

	@MsgbusMethod
	public void getMap(Request req, Response resp) {
		String guid = req.getString("guid");
		MapInfo map = mapApi.getMap(req.getOrgDomain(), guid);
		resp.put("map", PrimitiveConverter.serialize(map));
	}

	@MsgbusMethod
	public void createMap(Request req, Response resp) {
		MapInfo map = (MapInfo) PrimitiveConverter.overwrite(new MapInfo(), req.getParams(), conf.getParseCallback(req.getOrgDomain()));
		mapApi.createMap(req.getOrgDomain(), map);
		resp.put("guid", map.getGuid());
	}

	@MsgbusMethod
	public void updateMap(Request req, Response resp) {
		MapInfo before = mapApi.getMap(req.getOrgDomain(), req.getString("guid"));
		MapInfo map = (MapInfo) PrimitiveConverter.overwrite(before, req.getParams(), conf.getParseCallback(req.getOrgDomain()));
		mapApi.updateMap(req.getOrgDomain(), map);
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void removeMaps(Request req, Response resp) {
		Collection<String> guids = (Collection<String>) req.get("guids");
		mapApi.removeMaps(req.getOrgDomain(), guids);
	}

	@MsgbusMethod
	public void removeMap(Request req, Response resp) {
		String guid = req.getString("guid");
		mapApi.removeMap(req.getOrgDomain(), guid);
	}
}

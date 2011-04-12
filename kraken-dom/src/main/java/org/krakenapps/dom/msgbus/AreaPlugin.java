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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AreaApi;
import org.krakenapps.dom.model.Area;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-area-plugin")
@MsgbusPlugin
@JpaConfig(factory = "dom")
public class AreaPlugin {
	@Requires
	private AreaApi areaApi;

	@MsgbusMethod
	public void createArea(Request req, Response resp) {
		int parentId = req.getInteger("parentId");
		String name = req.getString("name");
		String description = req.getString("description");

		Area area = areaApi.createArea(req.getOrgId(), parentId, name, description);
		resp.put("id", area.getId());
	}

	@MsgbusMethod
	public void updateArea(Request req, Response resp) {
		int areaId = req.getInteger("id");
		String name = req.getString("name");
		String description = req.getString("description");

		areaApi.updateArea(req.getOrgId(), areaId, name, description);
	}

	@MsgbusMethod
	public void removeArea(Request req, Response resp) {
		int areaId = req.getInteger("id");

		areaApi.removeArea(req.getOrgId(), areaId);
		resp.put("id", areaId);
	}

	@MsgbusMethod
	public void getAreaTree(Request req, Response resp) {
		Map<String, Object> tree = getAreaTree(req.getOrgId());
		resp.put("areaTree", tree);
	}

	@Transactional
	private Map<String, Object> getAreaTree(int organizationId) {
		Area rootArea = areaApi.getRootArea(organizationId);
		return serializeAreaTree(rootArea);
	}

	private Map<String, Object> serializeAreaTree(Area area) {
		Map<String, Object> map = area.marshal();
		List<Object> subTree = new ArrayList<Object>();
		for (Area subArea : area.getAreas()) {
			subTree.add(serializeAreaTree(subArea));
		}

		map.put("children", subTree);
		return map;
	}
}

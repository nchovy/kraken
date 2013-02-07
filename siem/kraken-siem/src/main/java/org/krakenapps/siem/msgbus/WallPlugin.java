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
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.krakenapps.siem.ConfigManager;
import org.krakenapps.siem.model.WallPreset;

@Component(name = "siem-wall-plugin")
@MsgbusPlugin
public class WallPlugin {
	@Requires
	private ConfigManager cfg;

	@MsgbusMethod
	public void getPresetNames(Request req, Response resp) {
		ConfigCollection col = getCol(req);
		ConfigIterator it = col.findAll();
		try {
			List<Object> presets = new ArrayList<Object>();
			while (it.hasNext()) {
				WallPreset preset = PrimitiveConverter.parse(WallPreset.class, it.next().getDocument());

				Map<String, Object> m = new HashMap<String, Object>();
				m.put("guid", preset.getGuid());
				m.put("name", preset.getName());
				presets.add(m);
			}

			resp.put("presets", presets);
		} finally {
			if (it != null)
				it.close();
		}
	}

	@MsgbusMethod
	public void getPreset(Request req, Response resp) {
		ConfigCollection col = getCol(req);
		String guid = req.getString("guid");

		Config c = col.findOne(Predicates.field("guid", guid));
		resp.put("preset", c.getDocument());
	}

	@MsgbusMethod
	public void setPreset(Request req, Response resp) {
		ConfigCollection col = getCol(req);
		WallPreset p = PrimitiveConverter.parse(WallPreset.class, req.getParams());

		Config c = col.findOne(Predicates.field("guid", p.getGuid()));
		if (c == null) {
			col.add(PrimitiveConverter.serialize(p));
		} else {
			c.setDocument(PrimitiveConverter.serialize(p));
			col.update(c);
		}
	}

	@MsgbusMethod
	public void removePresets(Request req, Response resp) {
		@SuppressWarnings("unchecked")
		List<String> guids = (List<String>) req.get("guids");

		ConfigCollection col = getCol(req);
		for (String guid : guids) {
			Config c = col.findOne(Predicates.field("guid", guid));
			if (c != null)
				col.remove(c);
		}
	}

	@MsgbusMethod
	public void removePreset(Request req, Response resp) {
		ConfigCollection col = getCol(req);
		String guid = req.getString("guid");

		Config c = col.findOne(Predicates.field("guid", guid));
		if (c != null)
			col.remove(c);
	}

	private ConfigCollection getCol(Request req) {
		ConfigDatabase db = cfg.getDatabase(req.getSession());
		ConfigCollection col = db.ensureCollection("wall");
		return col;
	}

}

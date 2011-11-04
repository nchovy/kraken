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

		List<Object> presets = new ArrayList<Object>();
		while (it.hasNext()) {
			WallPreset preset = PrimitiveConverter.parse(WallPreset.class, (Map<String, Object>) it.next()
					.getDocument());

			Map<String, Object> m = new HashMap<String, Object>();
			m.put("guid", preset.getGuid());
			m.put("name", preset.getName());
			presets.add(m);
		}

		resp.put("presets", presets);
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

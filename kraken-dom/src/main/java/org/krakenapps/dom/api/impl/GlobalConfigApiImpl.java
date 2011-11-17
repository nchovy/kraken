package org.krakenapps.dom.api.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.GlobalConfigApi;
import org.krakenapps.dom.model.GlobalConfig;

@Component(name = "dom-global-config-api")
@Provides
public class GlobalConfigApiImpl implements GlobalConfigApi {
	private static final String DB_NAME = "kraken-dom";
	private static final Class<GlobalConfig> cls = GlobalConfig.class;

	@Requires
	private ConfigService confsvc;

	@Override
	public Map<String, Object> getConfigs() {
		return getConfigs(true);
	}

	@Override
	public Map<String, Object> getConfigs(boolean getHidden) {
		ConfigDatabase db = confsvc.getDatabase(DB_NAME);
		if (db == null)
			return null;
		Collection<GlobalConfig> configs = db.findAll(cls).getDocuments(cls);
		Map<String, Object> m = new HashMap<String, Object>();
		for (GlobalConfig config : configs) {
			if (!config.isHidden() || getHidden)
				m.put(config.getKey(), config.getValue());
		}
		return m;
	}

	@Override
	public Object getConfig(String key) {
		ConfigDatabase db = confsvc.getDatabase(DB_NAME);
		if (db == null)
			return null;
		Config c = db.findOne(cls, Predicates.field("key", key));
		return (c != null) ? c.getDocument(cls) : null;
	}

	@Override
	public void setConfig(String key, Object value) {
		setConfig(key, value, false);
	}

	@Override
	public void setConfig(String key, Object value, boolean isHidden) {
		ConfigDatabase db = confsvc.getDatabase(DB_NAME);
		if (db == null)
			db = confsvc.createDatabase(DB_NAME);
		Config c = db.findOne(cls, Predicates.field("key", key));
		if (c == null) {
			GlobalConfig param = new GlobalConfig();
			param.setKey(key);
			param.setValue(value);
			param.setHidden(isHidden);
			db.add(param);
		} else {
			GlobalConfig param = c.getDocument(cls);
			param.setValue(value);
			param.setHidden(isHidden);
			db.update(c, param);
		}
	}

	@Override
	public void unsetConfig(String key) {
		ConfigDatabase db = confsvc.getDatabase(DB_NAME);
		if (db == null)
			db = confsvc.createDatabase(DB_NAME);
		Config c = db.findOne(cls, Predicates.field("key", key));
		if (c == null)
			return;
		db.remove(c);
	}
}

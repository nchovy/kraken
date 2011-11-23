package org.krakenapps.dom.api.impl;

import java.util.Collection;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.MapApi;
import org.krakenapps.dom.model.MapInfo;

@Component(name = "dom-map-api")
@Provides
public class MapApiImpl extends DefaultEntityEventProvider<MapInfo> implements MapApi {
	private static final Class<MapInfo> cls = MapInfo.class;
	private static final String NOT_FOUND = "map-not-found";
	private static final String ALREADY_EXIST = "map-already-exist";

	@Requires
	private ConfigManager cfg;

	private Predicate getPred(String guid) {
		return Predicates.field("guid", guid);
	}

	@Override
	public Collection<MapInfo> getMaps(String domain) {
		return cfg.all(domain, cls);
	}

	@Override
	public MapInfo findMap(String domain, String guid) {
		return cfg.find(domain, cls, getPred(guid));
	}

	@Override
	public MapInfo getMap(String domain, String guid) {
		return cfg.get(domain, cls, getPred(guid), NOT_FOUND);
	}

	@Override
	public void createMap(String domain, MapInfo map) {
		cfg.add(domain, cls, getPred(map.getGuid()), map, ALREADY_EXIST, this);
	}

	@Override
	public void updateMap(String domain, MapInfo map) {
		cfg.update(domain, cls, getPred(map.getGuid()), map, NOT_FOUND, this);
	}

	@Override
	public void removeMap(String domain, String guid) {
		cfg.remove(domain, cls, getPred(guid), NOT_FOUND, this);
	}
}

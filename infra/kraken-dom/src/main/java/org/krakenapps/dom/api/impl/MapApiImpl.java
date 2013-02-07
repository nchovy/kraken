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
package org.krakenapps.dom.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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

	private List<Predicate> getPreds(List<MapInfo> maps) {
		if (maps == null)
			return new ArrayList<Predicate>();

		List<Predicate> preds = new ArrayList<Predicate>(maps.size());
		for (MapInfo map : maps)
			preds.add(getPred(map.getGuid()));
		return preds;
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
	public void createMaps(String domain, Collection<MapInfo> maps) {
		List<MapInfo> mapList = new ArrayList<MapInfo>(maps);
		cfg.adds(domain, cls, getPreds(mapList), mapList, ALREADY_EXIST, this);
	}

	@Override
	public void createMap(String domain, MapInfo map) {
		cfg.add(domain, cls, getPred(map.getGuid()), map, ALREADY_EXIST, this);
	}

	@Override
	public void updateMaps(String domain, Collection<MapInfo> maps) {
		List<MapInfo> mapList = new ArrayList<MapInfo>(maps);
		for (MapInfo map : mapList)
			map.setUpdated(new Date());
		cfg.updates(domain, cls, getPreds(mapList), mapList, NOT_FOUND, this);
	}

	@Override
	public void updateMap(String domain, MapInfo map) {
		map.setUpdated(new Date());
		cfg.update(domain, cls, getPred(map.getGuid()), map, NOT_FOUND, this);
	}

	@Override
	public void removeMaps(String domain, Collection<String> guids) {
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String guid : guids)
			preds.add(getPred(guid));
		cfg.removes(domain, cls, preds, NOT_FOUND, this);
	}

	@Override
	public void removeMap(String domain, String guid) {
		cfg.remove(domain, cls, getPred(guid), NOT_FOUND, this);
	}
}

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
package org.krakenapps.dom.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.AreaApi;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.model.Area;

@Component(name = "dom-area-api")
@Provides
public class AreaApiImpl extends DefaultEntityEventProvider<Area> implements AreaApi {
	private static final Class<Area> cls = Area.class;
	private static final String NOT_FOUND = "area-not-found";
	private static final String ALREADY_EXIST = "area-already-exist";

	@Requires
	private ConfigManager cfg;

	private Predicate getPred(String guid) {
		return Predicates.field("guid", guid);
	}

	private List<Predicate> getPreds(List<Area> areas) {
		if (areas == null)
			return new ArrayList<Predicate>();
		
		List<Predicate> preds = new ArrayList<Predicate>(areas.size());
		for (Area area : areas)
			preds.add(getPred(area.getGuid()));
		return preds;
	}

	@Override
	public Collection<Area> getRootAreas(String domain) {
		Collection<Area> areas = cfg.all(domain, cls, Predicates.field("parent", null));
		for (Area area : areas)
			area.setChildren(getChildrens(domain, area.getGuid()));
		return areas;
	}

	@Override
	public Area findArea(String domain, String guid) {
		Area area = cfg.find(domain, cls, getPred(guid));
		if (area == null)
			return null;
		area.setChildren(getChildrens(domain, area.getGuid()));
		return area;
	}

	@Override
	public Area getArea(String domain, String guid) {
		Area area = cfg.get(domain, cls, getPred(guid), NOT_FOUND);
		area.setChildren(getChildrens(domain, area.getGuid()));
		return area;
	}

	private List<Area> getChildrens(String domain, String guid) {
		Collection<Area> areas = cfg.all(domain, cls, Predicates.field("parent", guid));
		for (Area area : areas)
			area.setChildren(getChildrens(domain, area.getGuid()));
		return (List<Area>) areas;
	}

	@Override
	public void createAreas(String domain, Collection<Area> areas) {
		List<Area> areaList = new ArrayList<Area>(areas);
		cfg.adds(domain, cls, getPreds(areaList), areaList, ALREADY_EXIST, this);
	}

	@Override
	public void createArea(String domain, Area area) {
		cfg.add(domain, cls, getPred(area.getGuid()), area, ALREADY_EXIST, this);
	}

	@Override
	public void updateAreas(String domain, Collection<Area> areas) {
		List<Area> areaList = new ArrayList<Area>(areas);
		for (Area area : areas)
			area.setUpdated(new Date());
		cfg.updates(domain, cls, getPreds(areaList), areaList, NOT_FOUND, this);
	}

	@Override
	public void updateArea(String domain, Area area) {
		area.setUpdated(new Date());
		cfg.update(domain, cls, getPred(area.getGuid()), area, NOT_FOUND, this);
	}

	@Override
	public void removeAreas(String domain, Collection<String> guids) {
		removeAreas(domain, guids, false);
	}

	@Override
	public void removeArea(String domain, String guid) {
		removeArea(domain, guid, false);
	}

	@Override
	public void removeAreas(String domain, Collection<String> guids, boolean removeHost) {
		Set<String> areaGuids = new HashSet<String>();
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String guid : guids) {
			if (areaGuids.contains(guid))
				continue;

			List<Area> areas = getAreaTree(getArea(domain, guid));
			for (Area area : areas)
				areaGuids.add(area.getGuid());
			preds.addAll(getPreds(areas));
		}
		cfg.removes(domain, cls, preds, NOT_FOUND, this, removeHost, null);
	}

	@Override
	public void removeArea(String domain, String guid, boolean removeHost) {
		List<Area> areas = getAreaTree(getArea(domain, guid));
		cfg.removes(domain, cls, getPreds(areas), NOT_FOUND, this, removeHost, null);
	}

	private List<Area> getAreaTree(Area area) {
		List<Area> areas = new ArrayList<Area>();
		for (Area child : area.getChildren())
			areas.addAll(getAreaTree(child));
		areas.add(area);
		return areas;
	}
}

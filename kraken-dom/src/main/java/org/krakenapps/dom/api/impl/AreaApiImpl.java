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

import java.util.Collection;
import java.util.Date;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.AreaApi;
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

	@Override
	public Collection<Area> getAreas(String domain) {
		return cfg.all(domain, cls);
	}

	@Override
	public Collection<Area> getRootAreas(String domain) {
		return cfg.all(domain, cls, Predicates.field("parent", null));
	}

	@Override
	public Area findArea(String domain, String guid) {
		return cfg.find(domain, cls, getPred(guid));
	}

	@Override
	public Area getArea(String domain, String guid) {
		return cfg.get(domain, cls, getPred(guid), NOT_FOUND);
	}

	@Override
	public void createArea(String domain, Area area) {
		cfg.add(domain, cls, getPred(area.getGuid()), area, ALREADY_EXIST, this);
	}

	@Override
	public void updateArea(String domain, Area area) {
		area.setUpdated(new Date());
		cfg.update(domain, cls, getPred(area.getGuid()), area, NOT_FOUND, this);
	}

	@Override
	public void removeArea(String domain, String guid) {
		cfg.remove(domain, cls, getPred(guid), NOT_FOUND, this);
	}
}

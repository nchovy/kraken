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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.TimetableApi;
import org.krakenapps.dom.model.Timetable;

@Component(name = "dom-timetable-api")
@Provides
public class TimetableApiImpl extends DefaultEntityEventProvider<Timetable> implements TimetableApi {
	private static final Class<Timetable> cls = Timetable.class;
	private static final String NOT_FOUND = "timetable-not-found";
	private static final String ALREADY_EXIST = "timetable-already-exist";

	@Requires
	private ConfigManager cfg;

	private Predicate getPred(String guid) {
		return Predicates.field("guid", guid);
	}

	@Override
	public Collection<Timetable> getTimetables(String domain) {
		return cfg.ensureCollection(domain, cls).findAll().getDocuments(cls);
	}

	@Override
	public Timetable findTimetable(String domain, String guid) {
		return cfg.find(domain, cls, getPred(guid));
	}

	@Override
	public Timetable getTimetable(String domain, String guid) {
		return cfg.get(domain, cls, getPred(guid), NOT_FOUND);
	}

	@Override
	public void createTimetable(String domain, Timetable timetable) {
		cfg.add(domain, cls, getPred(timetable.getGuid()), timetable, ALREADY_EXIST, this);
	}

	@Override
	public void updateTimetable(String domain, Timetable timetable) {
		cfg.update(domain, cls, getPred(timetable.getGuid()), timetable, NOT_FOUND, this);
	}

	@Override
	public void removeTimetable(String domain, String guid) {
		cfg.remove(domain, cls, getPred(guid), NOT_FOUND, this);
	}
}

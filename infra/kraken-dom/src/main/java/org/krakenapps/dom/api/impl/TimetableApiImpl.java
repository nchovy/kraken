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
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
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

	private List<Predicate> getPreds(List<Timetable> timetables) {
		if (timetables == null)
			return new ArrayList<Predicate>();
		
		List<Predicate> preds = new ArrayList<Predicate>(timetables.size());
		for (Timetable timetable : timetables)
			preds.add(getPred(timetable.getGuid()));
		return preds;
	}

	@Override
	public Collection<Timetable> getTimetables(String domain) {
		return cfg.all(domain, cls);
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
	public void createTimetables(String domain, Collection<Timetable> timetables) {
		List<Timetable> timetableList = new ArrayList<Timetable>(timetables);
		cfg.adds(domain, cls, getPreds(timetableList), timetableList, ALREADY_EXIST, this);
	}

	@Override
	public void createTimetable(String domain, Timetable timetable) {
		cfg.add(domain, cls, getPred(timetable.getGuid()), timetable, ALREADY_EXIST, this);
	}

	@Override
	public void updateTimetables(String domain, Collection<Timetable> timetables) {
		List<Timetable> timetableList = new ArrayList<Timetable>(timetables);
		for (Timetable timetable : timetableList)
			timetable.setUpdated(new Date());
		cfg.updates(domain, cls, getPreds(timetableList), timetableList, NOT_FOUND, this);
	}

	@Override
	public void updateTimetable(String domain, Timetable timetable) {
		timetable.setUpdated(new Date());
		cfg.update(domain, cls, getPred(timetable.getGuid()), timetable, NOT_FOUND, this);
	}

	@Override
	public void removeTimetables(String domain, Collection<String> guids) {
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String guid : guids)
			preds.add(getPred(guid));
		cfg.removes(domain, cls, preds, NOT_FOUND, this);
	}

	@Override
	public void removeTimetable(String domain, String guid) {
		cfg.remove(domain, cls, getPred(guid), NOT_FOUND, this);
	}
}

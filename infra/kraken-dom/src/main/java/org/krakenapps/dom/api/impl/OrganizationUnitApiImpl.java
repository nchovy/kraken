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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.model.OrganizationUnit;

@Component(name = "dom-org-unit-api")
@Provides
public class OrganizationUnitApiImpl extends DefaultEntityEventProvider<OrganizationUnit> implements OrganizationUnitApi {
	private static final Class<OrganizationUnit> cls = OrganizationUnit.class;
	private static final String NOT_FOUND = "org-unit-not-found";
	private static final String ALREADY_EXIST = "org-unit-already-exist";

	@Requires
	private ConfigManager cfg;

	private Predicate getPred(String guid) {
		return Predicates.field("guid", guid);
	}

	private List<Predicate> getPreds(List<OrganizationUnit> orgUnits) {
		if (orgUnits == null)
			return new ArrayList<Predicate>();

		List<Predicate> preds = new ArrayList<Predicate>(orgUnits.size());
		for (OrganizationUnit orgUnit : orgUnits)
			preds.add(getPred(orgUnit.getGuid()));
		return preds;
	}

	@Override
	public Collection<OrganizationUnit> getOrganizationUnits(String domain) {
		return getOrganizationUnits(domain, true);
	}

	@Override
	public Collection<OrganizationUnit> getOrganizationUnits(String domain, boolean includeChildren) {
		Collection<OrganizationUnit> orgUnits = cfg.all(domain, cls);
		if (!includeChildren)
			return orgUnits;

		for (OrganizationUnit orgUnit : orgUnits)
			orgUnit.setChildren(getChildrens(domain, orgUnit.getGuid()));
		return orgUnits;
	}

	@Override
	public OrganizationUnit findOrganizationUnit(String domain, String guid) {
		OrganizationUnit orgUnit = cfg.find(domain, cls, getPred(guid));
		if (orgUnit == null)
			return null;
		orgUnit.setChildren(getChildrens(domain, orgUnit.getGuid()));
		return orgUnit;
	}

	@Override
	public OrganizationUnit getOrganizationUnit(String domain, String guid) {
		OrganizationUnit orgUnit = cfg.get(domain, cls, getPred(guid), NOT_FOUND);
		orgUnit.setChildren(getChildrens(domain, orgUnit.getGuid()));
		return orgUnit;
	}

	@Override
	public OrganizationUnit findOrganizationUnitByName(String domain, String... names) {
		OrganizationUnit orgUnit = null;
		String parentGuid = null;

		for (String name : names) {
			Map<String, Object> terms = new HashMap<String, Object>();
			terms.put("name", name);
			terms.put("parent", parentGuid);
			orgUnit = cfg.find(domain, OrganizationUnit.class, Predicates.field(terms));
			if (orgUnit == null)
				return null;
			parentGuid = orgUnit.getGuid();
		}
		orgUnit.setChildren(getChildrens(domain, orgUnit.getGuid()));

		return orgUnit;
	}

	private List<OrganizationUnit> getChildrens(String domain, String guid) {
		Collection<OrganizationUnit> orgUnits = cfg.all(domain, cls, Predicates.field("parent", guid));
		for (OrganizationUnit orgUnit : orgUnits)
			orgUnit.setChildren(getChildrens(domain, orgUnit.getGuid()));
		return (List<OrganizationUnit>) orgUnits;
	}

	@Override
	public void createOrganizationUnits(String domain, Collection<OrganizationUnit> orgUnits) {
		List<OrganizationUnit> orgUnitList = new ArrayList<OrganizationUnit>(orgUnits);
		cfg.adds(domain, cls, getPreds(orgUnitList), orgUnitList, ALREADY_EXIST, this);
	}

	@Override
	public void createOrganizationUnit(String domain, OrganizationUnit orgUnit) {
		cfg.add(domain, cls, getPred(orgUnit.getGuid()), orgUnit, ALREADY_EXIST, this);
	}

	@Override
	public void updateOrganizationUnits(String domain, Collection<OrganizationUnit> orgUnits) {
		List<OrganizationUnit> orgUnitList = new ArrayList<OrganizationUnit>(orgUnits);
		for (OrganizationUnit orgUnit : orgUnitList)
			orgUnit.setUpdated(new Date());
		cfg.updates(domain, cls, getPreds(orgUnitList), orgUnitList, NOT_FOUND, this);
	}

	@Override
	public void updateOrganizationUnit(String domain, OrganizationUnit orgUnit) {
		orgUnit.setUpdated(new Date());
		cfg.update(domain, cls, getPred(orgUnit.getGuid()), orgUnit, NOT_FOUND, this);
	}

	@Override
	public void removeOrganizationUnits(String domain, Collection<String> guids) {
		removeOrganizationUnits(domain, guids, false);
	}

	@Override
	public void removeOrganizationUnits(String domain, Collection<String> guids, boolean moveUser) {
		Set<String> orgUnitGuids = new HashSet<String>();
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String guid : guids) {
			if (orgUnitGuids.contains(guid))
				continue;

			List<OrganizationUnit> orgUnits = getOrganizationUnitTree(getOrganizationUnit(domain, guid));
			for (OrganizationUnit orgUnit : orgUnits)
				orgUnitGuids.add(orgUnit.getGuid());
			preds.addAll(getPreds(orgUnits));
		}

		cfg.removes(domain, cls, preds, NOT_FOUND, this, moveUser, null);
	}

	@Override
	public void removeOrganizationUnit(String domain, String guid) {
		removeOrganizationUnit(domain, guid, false);
	}

	@Override
	public void removeOrganizationUnit(String domain, String guid, boolean moveUser) {
		List<OrganizationUnit> orgUnits = getOrganizationUnitTree(getOrganizationUnit(domain, guid));
		cfg.removes(domain, cls, getPreds(orgUnits), NOT_FOUND, this, moveUser, null);
	}

	private List<OrganizationUnit> getOrganizationUnitTree(OrganizationUnit orgUnit) {
		List<OrganizationUnit> orgUnits = new ArrayList<OrganizationUnit>();
		for (OrganizationUnit child : orgUnit.getChildren())
			orgUnits.addAll(getOrganizationUnitTree(child));
		orgUnits.add(orgUnit);
		return orgUnits;
	}
}

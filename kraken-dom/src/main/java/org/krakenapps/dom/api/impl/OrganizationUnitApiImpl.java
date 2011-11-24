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
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.ConfigManager;
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

	@Override
	public Collection<OrganizationUnit> getOrganizationUnits(String domain) {
		return cfg.all(domain, cls);
	}

	@Override
	public OrganizationUnit findOrganizationUnit(String domain, String guid) {
		return cfg.find(domain, cls, getPred(guid));
	}

	@Override
	public OrganizationUnit getOrganizationUnit(String domain, String guid) {
		return cfg.get(domain, cls, getPred(guid), NOT_FOUND);
	}

	@Override
	public void createOrganizationUnit(String domain, OrganizationUnit orgUnit) {
		cfg.add(domain, cls, getPred(orgUnit.getGuid()), orgUnit, ALREADY_EXIST, this);
	}

	@Override
	public void updateOrganizationUnit(String domain, OrganizationUnit orgUnit) {
		orgUnit.setUpdated(new Date());
		cfg.update(domain, cls, getPred(orgUnit.getGuid()), orgUnit, NOT_FOUND, this);
	}

	@Override
	public void removeOrganizationUnit(String domain, String guid) {
		cfg.remove(domain, cls, getPred(guid), NOT_FOUND, this);
	}
}

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
package org.krakenapps.dom.api;

import java.util.Collection;

import org.krakenapps.dom.model.OrganizationUnit;

public interface OrganizationUnitApi extends EntityEventProvider<OrganizationUnit> {
	Collection<OrganizationUnit> getOrganizationUnits(String domain);

	Collection<OrganizationUnit> getOrganizationUnits(String domain, boolean includeChildren);

	OrganizationUnit findOrganizationUnit(String domain, String guid);

	OrganizationUnit getOrganizationUnit(String domain, String guid);

	OrganizationUnit findOrganizationUnitByName(String domain, String... names);

	void createOrganizationUnits(String domain, Collection<OrganizationUnit> orgUnits);

	void createOrganizationUnit(String domain, OrganizationUnit orgUnit);

	void updateOrganizationUnits(String domain, Collection<OrganizationUnit> orgUnits);

	void updateOrganizationUnit(String domain, OrganizationUnit orgUnit);

	void removeOrganizationUnits(String domain, Collection<String> guids);

	void removeOrganizationUnits(String domain, Collection<String> guids, boolean moveUser);

	void removeOrganizationUnit(String domain, String guid);

	void removeOrganizationUnit(String domain, String guid, boolean moveUser);
}

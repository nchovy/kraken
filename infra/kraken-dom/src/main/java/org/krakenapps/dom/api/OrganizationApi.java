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

import java.util.Map;

import org.krakenapps.dom.model.Organization;

public interface OrganizationApi extends EntityEventProvider<Organization> {
	Organization findOrganization(String domain);

	Organization getOrganization(String domain);

	void createOrganization(Organization organization);

	void updateOrganization(Organization organization);

	void removeOrganization(String domain);

	Map<String, Object> getOrganizationParameters(String domain);

	Object getOrganizationParameter(String domain, String key);

	<T> T getOrganizationParameter(String domain, String key, Class<T> cls);

	void setOrganizationParameter(String domain, String key, Object value);

	void unsetOrganizationParameter(String domain, String key);
}

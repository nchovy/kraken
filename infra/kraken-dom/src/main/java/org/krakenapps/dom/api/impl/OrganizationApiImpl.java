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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.dom.api.DOMException;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.model.Organization;

@Component(name = "dom-org-api")
@Provides
public class OrganizationApiImpl extends DefaultEntityEventProvider<Organization> implements OrganizationApi {
	private static final String DB_PREFIX = "kraken-dom-";
	private static final Class<Organization> cls = Organization.class;
	private static final String NOT_FOUND = "organization-not-found";
	private static final String ALREADY_EXIST = "organization-already-exist";

	@Requires
	private ConfigManager cfg;

	@Requires
	private ConfigService confsvc;

	public void setConfigManager(ConfigManager cfg) {
		this.cfg = cfg;
	}

	public void setConfigService(ConfigService conf) {
		this.confsvc = conf;
	}

	@Override
	public Organization findOrganization(String domain) {
		ConfigDatabase db = cfg.findDatabase(domain);
		if (db == null)
			return null;
		Config c = db.findOne(cls, null);
		return (c != null) ? c.getDocument(cls) : null;
	}

	@Override
	public Organization getOrganization(String domain) {
		Organization org = findOrganization(domain);
		if (org == null)
			throw new DOMException(NOT_FOUND);
		return org;
	}

	@Override
	public void createOrganization(Organization organization) {
		ConfigDatabase db = cfg.findDatabase(organization.getDomain());
		if (db != null)
			throw new DOMException(ALREADY_EXIST);
		db = confsvc.createDatabase(DB_PREFIX + organization.getDomain());
		db.add(organization);
		fireEntityAdded(organization.getDomain(), organization);
	}

	@Override
	public void updateOrganization(Organization organization) {
		ConfigDatabase db = cfg.getDatabase(organization.getDomain());
		Config c = db.findOne(cls, null);
		if (c != null) {
			organization.setUpdated(new Date());
			db.update(c, organization);
			fireEntityUpdated(organization.getDomain(), organization);
		} else {
			db.add(organization);
			fireEntityAdded(organization.getDomain(), organization);
		}
	}

	@Override
	public void removeOrganization(String domain) {
		Config c = cfg.getDatabase(domain).findOne(cls, null);
		Organization organization = null;
		if (c != null)
			organization = c.getDocument(cls);
		confsvc.dropDatabase(DB_PREFIX + domain);
		if (organization != null)
			fireEntityRemoved(organization.getDomain(), organization);
	}

	@Override
	public Map<String, Object> getOrganizationParameters(String domain) {
		return getOrganization(domain).getParameters();
	}

	@Override
	public Object getOrganizationParameter(String domain, String key) {
		Map<String, Object> params = getOrganization(domain).getParameters();
		return (params != null) ? params.get(key) : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getOrganizationParameter(String domain, String key, Class<T> cls) {
		Object param = getOrganizationParameter(domain, key);
		try {
			return (T) param;
		} catch (ClassCastException e) {
			return null;
		}
	}

	@Override
	public void setOrganizationParameter(String domain, String key, Object value) {
		Organization organization = getOrganization(domain);
		Map<String, Object> params = organization.getParameters();
		if (params == null)
			params = new HashMap<String, Object>();
		params.put(key, value);
		organization.setParameters(params);
		organization.setUpdated(new Date());
		cfg.update(domain, cls, null, organization, NOT_FOUND, this);
		fireEntityUpdated(organization.getDomain(), organization);
	}

	@Override
	public void unsetOrganizationParameter(String domain, String key) {
		Organization organization = getOrganization(domain);
		Map<String, Object> params = organization.getParameters();
		if (params != null)
			params.remove(key);
		organization.setParameters(params);
		organization.setUpdated(new Date());
		cfg.update(domain, cls, null, organization, NOT_FOUND, this);
		fireEntityUpdated(organization.getDomain(), organization);
	}
}
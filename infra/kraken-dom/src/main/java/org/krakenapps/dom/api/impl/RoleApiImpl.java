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
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.Permission;
import org.krakenapps.dom.model.Role;

@Component(name = "dom-role-api")
@Provides
public class RoleApiImpl extends DefaultEntityEventProvider<Role> implements RoleApi {
	private static final Class<Role> cls = Role.class;
	private static final String NOT_FOUND = "role-not-found";
	private static final String ALREADY_EXIST = "role-already-exist";

	@Requires
	private ConfigManager cfg;

	@Requires
	private AdminApi adminApi;

	private Predicate getPred(String name) {
		return Predicates.field("name", name);
	}

	private List<Predicate> getPreds(List<Role> roles) {
		if (roles == null)
			return new ArrayList<Predicate>();
		
		List<Predicate> preds = new ArrayList<Predicate>(roles.size());
		for (Role role : roles)
			preds.add(getPred(role.getName()));
		return preds;
	}

	@Override
	public Collection<Role> getRoles(String domain) {
		return cfg.all(domain, cls);
	}

	@Override
	public Collection<Role> getGrantableRoles(String domain, String loginName) {
		Collection<Role> roles = new ArrayList<Role>();

		Admin admin = adminApi.getAdmin(domain, loginName);
		for (Role role : getRoles(domain)) {
			if (role.getLevel() < admin.getRole().getLevel())
				roles.add(role);
		}

		return roles;
	}

	@Override
	public Role findRole(String domain, String name) {
		return cfg.find(domain, cls, getPred(name));
	}

	@Override
	public Role getRole(String domain, String name) {
		return cfg.get(domain, cls, getPred(name), NOT_FOUND);
	}

	@Override
	public void createRoles(String domain, Collection<Role> roles) {
		List<Role> roleList = new ArrayList<Role>(roles);
		cfg.adds(domain, cls, getPreds(roleList), roleList, ALREADY_EXIST, this);
	}

	@Override
	public void createRole(String domain, Role role) {
		cfg.add(domain, cls, getPred(role.getName()), role, ALREADY_EXIST, this);
	}

	@Override
	public void updateRoles(String domain, Collection<Role> roles) {
		List<Role> roleList = new ArrayList<Role>(roles);
		for (Role role : roleList)
			role.setUpdated(new Date());
		cfg.updates(domain, cls, getPreds(roleList), roleList, NOT_FOUND, this);
	}

	@Override
	public void updateRole(String domain, Role role) {
		role.setUpdated(new Date());
		cfg.update(domain, cls, getPred(role.getName()), role, NOT_FOUND, this);
	}

	@Override
	public void removeRoles(String domain, Collection<String> names) {
		List<Predicate> preds = new ArrayList<Predicate>();
		for (String name : names)
			preds.add(getPred(name));
		cfg.removes(domain, cls, preds, NOT_FOUND, this);

		for (Admin admin : adminApi.getAdmins(domain)) {
			if (names.contains(admin.getRole().getName()))
				adminApi.unsetAdmin(domain, null, admin.getUser().getLoginName());
		}
	}

	@Override
	public void removeRole(String domain, String name) {
		cfg.remove(domain, cls, getPred(name), NOT_FOUND, this);

		for (Admin admin : adminApi.getAdmins(domain)) {
			if (name.equals(admin.getRole().getName()))
				adminApi.unsetAdmin(domain, null, admin.getUser().getLoginName());
		}
	}

	@Override
	public boolean hasPermission(String domain, String loginName, String group, String permission) {
		Admin admin = adminApi.getAdmin(domain, loginName);
		for (Permission perm : admin.getRole().getPermissions()) {
			if (perm.getGroup().equals(group) && perm.getPermission().equals(permission))
				return true;
		}
		return false;
	}
}

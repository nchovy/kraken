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

import org.krakenapps.dom.model.Role;

public interface RoleApi extends EntityEventProvider<Role> {
	Collection<Role> getRoles(String domain);

	Collection<Role> getGrantableRoles(String domain, String loginName);

	Role findRole(String domain, String name);

	Role getRole(String domain, String name);

	void createRoles(String domain, Collection<Role> roles);

	void createRole(String domain, Role role);

	void updateRoles(String domain, Collection<Role> roles);

	void updateRole(String domain, Role role);

	void removeRoles(String domain, Collection<String> names);

	void removeRole(String domain, String name);

	boolean hasPermission(String domain, String loginName, String group, String permission);
}

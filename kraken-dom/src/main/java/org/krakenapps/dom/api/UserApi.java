/*
 * Copyright 2011 Future Systems
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

import org.krakenapps.dom.model.User;

public interface UserApi extends EntityEventProvider<User> {
	Collection<UserExtensionProvider> getExtensionProviders();

	UserExtensionProvider getExtensionProvider(String name);

	Collection<User> getUsers();

	Collection<User> getUsers(int orgId, Collection<Integer> idList);

	Collection<User> getUsers(int orgId);

	Collection<User> getUsers(int orgId, Integer orgUnitId, boolean includeChildren);

	Collection<User> getUsers(String domainController);

	User getUser(int id);
	
	User getUserByLoginName(String loginName);

	void createUser(User user);

	void updateUser(User user);

	void removeUser(int id);
	
	boolean verifyPassword(String id, String password);

	String hashPassword(String salt, String text);
}

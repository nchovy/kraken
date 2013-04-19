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
package org.krakenapps.ldap;

import java.util.Collection;

import com.novell.ldap.LDAPConnection;

public interface LdapService {
	Collection<LdapProfile> getProfiles();

	LdapProfile getProfile(String name);

	void createProfile(LdapProfile profile);

	void updateProfile(LdapProfile profile);

	void removeProfile(String name);

	Collection<LdapUser> getUsers(LdapProfile profile);

	LdapUser findUser(LdapProfile profile, String uid);

	Collection<LdapOrgUnit> getOrgUnits(LdapProfile profile);

	boolean verifyPassword(LdapProfile profile, String uid, String password);

	boolean verifyPassword(LdapProfile profile, String uid, String password, int timeout);

	void testLdapConnection(LdapProfile profile, Integer timeout);

	void changePassword(LdapProfile profile, String uid, String newPassword);

	void changePassword(LdapProfile profile, String uid, String newPassword, int timeout);

	LDAPConnection openLdapConnection(LdapProfile profile, Integer timeout);
}

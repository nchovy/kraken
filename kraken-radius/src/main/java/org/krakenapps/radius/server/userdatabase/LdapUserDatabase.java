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
package org.krakenapps.radius.server.userdatabase;

import org.krakenapps.ldap.LdapProfile;
import org.krakenapps.ldap.LdapService;
import org.krakenapps.radius.server.ConfigurableUserDatabase;
import org.krakenapps.radius.server.RadiusConfigurator;
import org.krakenapps.radius.server.RadiusUserDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapUserDatabase extends ConfigurableUserDatabase {
	private final Logger logger = LoggerFactory.getLogger(LdapUserDatabase.class.getName());
	private LdapService ldap;

	public LdapUserDatabase(String name, RadiusUserDatabaseFactory factory, RadiusConfigurator config, LdapService ldap) {
		super(name, factory, config);
		this.ldap = ldap;
	}

	@Override
	public boolean authenticate(String userName, String password) {
		String profileName = (String) getConfig("profile_name");
		LdapProfile profile = ldap.getProfile(profileName);
		if (profile == null) {
			logger.error("kraken radius: ldap profile not found - {}", profileName);
			return false;
		}
		
		return ldap.verifyPassword(profile, userName, password);
	}
}

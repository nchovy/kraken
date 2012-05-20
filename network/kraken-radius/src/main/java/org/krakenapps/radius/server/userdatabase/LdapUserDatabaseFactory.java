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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.ldap.LdapService;
import org.krakenapps.radius.server.RadiusConfigMetadata;
import org.krakenapps.radius.server.RadiusInstanceConfig;
import org.krakenapps.radius.server.RadiusModuleType;
import org.krakenapps.radius.server.RadiusUserDatabase;
import org.krakenapps.radius.server.RadiusUserDatabaseFactory;
import org.krakenapps.radius.server.RadiusConfigMetadata.Type;

@Component(name = "radius-ldap-udf")
@Provides
public class LdapUserDatabaseFactory implements RadiusUserDatabaseFactory {

	@Requires
	private LdapService ldap;

	private List<RadiusConfigMetadata> configMetadatas;

	@Override
	public String getName() {
		return "ldap";
	}

	@Override
	public RadiusModuleType getModuleType() {
		return RadiusModuleType.UserDatabase;
	}

	public LdapUserDatabaseFactory() {
		configMetadatas = new ArrayList<RadiusConfigMetadata>();
		RadiusConfigMetadata profileName = new RadiusConfigMetadata(Type.String, "ldap_profile_name", true);
		configMetadatas.add(profileName);
		configMetadatas = Collections.unmodifiableList(configMetadatas);
	}

	@Override
	public List<RadiusConfigMetadata> getConfigMetadatas() {
		return configMetadatas;
	}

	@Override
	public RadiusUserDatabase newInstance(RadiusInstanceConfig config) {
		config.verify(configMetadatas);
		String ldapProfileName = (String) config.getConfigs().get("ldap_profile_name");
		return new LdapUserDatabase(config.getName(), this, ldap, ldapProfileName);
	}

	@Override
	public String toString() {
		return "LDAP user database";
	}
}

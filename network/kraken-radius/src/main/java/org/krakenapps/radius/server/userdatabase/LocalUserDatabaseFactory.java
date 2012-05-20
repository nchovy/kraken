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
import org.krakenapps.radius.server.RadiusConfigMetadata;
import org.krakenapps.radius.server.RadiusInstanceConfig;
import org.krakenapps.radius.server.RadiusModuleType;
import org.krakenapps.radius.server.RadiusUserDatabase;
import org.krakenapps.radius.server.RadiusUserDatabaseFactory;

@Component(name = "radius-local-udf")
@Provides
public class LocalUserDatabaseFactory implements RadiusUserDatabaseFactory {

	@Requires
	private LocalUserRegistry userRegistry;

	private List<RadiusConfigMetadata> configMetadatas;

	public LocalUserDatabaseFactory() {
		configMetadatas = new ArrayList<RadiusConfigMetadata>();
		configMetadatas = Collections.unmodifiableList(configMetadatas);
	}

	@Override
	public String getName() {
		return "local";
	}

	@Override
	public RadiusModuleType getModuleType() {
		return RadiusModuleType.UserDatabase;

	}

	@Override
	public List<RadiusConfigMetadata> getConfigMetadatas() {
		return configMetadatas;
	}

	@Override
	public RadiusUserDatabase newInstance(RadiusInstanceConfig config) {
		config.verify(configMetadatas);
		return new LocalUserDatabase(config.getName(), this, userRegistry);
	}

	@Override
	public String toString() {
		return "Local User Database";
	}
}

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
package org.krakenapps.radius.server.auth;

import java.util.List;

import org.krakenapps.radius.protocol.AccessRequest;
import org.krakenapps.radius.protocol.RadiusPacket;
import org.krakenapps.radius.server.ConfigurableAuthenticator;
import org.krakenapps.radius.server.RadiusAuthenticatorFactory;
import org.krakenapps.radius.server.RadiusConfigurator;
import org.krakenapps.radius.server.RadiusUserDatabase;

public class PapAuthenticator extends ConfigurableAuthenticator {

	public PapAuthenticator(String name, RadiusAuthenticatorFactory factory, RadiusConfigurator config) {
		super(name, factory, config);
	}

	@Override
	public RadiusPacket authenticate(AccessRequest req, List<RadiusUserDatabase> userDatabases) {
		return null;
	}

}

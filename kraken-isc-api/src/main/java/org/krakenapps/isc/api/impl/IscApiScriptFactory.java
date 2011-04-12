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
package org.krakenapps.isc.api.impl;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.isc.api.IscClient;
import org.krakenapps.isc.api.IscClientConfig;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class IscApiScriptFactory implements ScriptFactory {
	private IscClientConfig config;
	private BundleContext bc;

	public IscApiScriptFactory(BundleContext bc) {
		this.bc = bc;
	}

	@Override
	public Script createScript() {
		IscClient client = getClient();
		return new IscApiScript(client, config);
	}

	private IscClient getClient() {
		ServiceReference ref = bc.getServiceReference(IscClient.class.getName());
		if (ref != null)
			return (IscClient) bc.getService(ref);
		return null;
	}

}

/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.servlet.json.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.servlet.json.JsonHttpScript;
import org.krakenapps.servlet.json.JsonHttpServiceApi;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@Component(name = "json-http-script-factory")
@Provides
public class JsonHttpScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "json-servlet")
	private String alias;

	private BundleContext context;

	public JsonHttpScriptFactory(BundleContext context) {
		this.context = context;
	}

	@Override
	public Script createScript() {
		ServiceReference ref = context.getServiceReference(JsonHttpServiceApi.class.getName());
		JsonHttpServiceApi manager = (JsonHttpServiceApi) context.getService(ref);
		return new JsonHttpScript(manager);
	}

}

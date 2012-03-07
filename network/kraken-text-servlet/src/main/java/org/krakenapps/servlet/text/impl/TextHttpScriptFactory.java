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
package org.krakenapps.servlet.text.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.servlet.text.TextHttpServiceApi;
import org.krakenapps.servlet.text.TextHttpScript;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@Component(name = "text-http-script-factory")
@Provides
public class TextHttpScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "text-servlet")
	private String alias;

	private BundleContext bc;

	public TextHttpScriptFactory(BundleContext bc) {
		this.bc = bc;
	}

	@Override
	public Script createScript() {
		ServiceReference ref = bc.getServiceReference(TextHttpServiceApi.class.getName());
		TextHttpServiceApi manager = (TextHttpServiceApi) bc.getService(ref);
		return new TextHttpScript(manager);
	}

}

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
package org.krakenapps.console;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class ConsoleAutoComplete {
	private BundleContext bundleContext;

	public ConsoleAutoComplete(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	public List<String> search(String prefix) {
		List<String> terms = new ArrayList<String>();
		if (!isFirstTerm(prefix)) {
			return terms;
		}

		if (isScriptFactoryDetermined(prefix)) {
			addScriptMethods(prefix, terms);
		} else {
			addScriptAliases(prefix, terms);
		}

		Collections.sort(terms);

		return terms;
	}

	private boolean isFirstTerm(String prefix) {
		return prefix.indexOf(' ') < 0;
	}

	private boolean isScriptFactoryDetermined(String prefix) {
		return prefix.indexOf('.') >= 0;
	}

	private void addScriptMethods(String prefix, List<String> terms) {
		try {
			String token = prefix.split(" ")[0];
			int dotPos = token.indexOf('.');
			String alias = token.substring(0, dotPos);
			String methodPrefix = token.substring(dotPos + 1);

			ServiceReference[] refs = bundleContext.getServiceReferences(ScriptFactory.class.getName(), "(alias="
					+ alias + ")");
			if (refs == null || refs.length == 0) {
				return;
			}

			ScriptFactory scriptFactory = (ScriptFactory) bundleContext.getService(refs[0]);
			Script script = scriptFactory.createScript();
			for (Method m : script.getClass().getMethods()) {
				Class<?>[] paramTypes = m.getParameterTypes();
				if (paramTypes == null || paramTypes.length == 0) {
					continue;
				}

				if (!paramTypes[0].isArray())
					continue;

				if (methodPrefix.length() == 0 || (methodPrefix.length() > 0 && m.getName().startsWith(methodPrefix))) {
					terms.add(m.getName());
				}
			}

		} catch (InvalidSyntaxException e) {
			// ignore
		} catch (NullPointerException e) {
			// ignore
		}
	}

	private void addScriptAliases(String prefix, List<String> terms) {
		try {
			ServiceReference[] refs = bundleContext.getServiceReferences(ScriptFactory.class.getName(), null);
			if (refs == null)
				return;

			for (int i = 0; i < refs.length; i++) {
				if (refs[i].getProperty("alias") != null) {
					String alias = refs[i].getProperty("alias").toString();
					if (alias.startsWith(prefix))
						terms.add(alias);
				}
			}

		} catch (InvalidSyntaxException e) {
			// ignore
		}
	}
}

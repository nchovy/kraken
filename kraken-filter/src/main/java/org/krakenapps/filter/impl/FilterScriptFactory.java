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
package org.krakenapps.filter.impl;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.filter.FilterManager;
import org.krakenapps.filter.FilterScript;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The script factory class for filter management.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class FilterScriptFactory implements ScriptFactory {
	private BundleContext context;

	public FilterScriptFactory(BundleContext context) {
		this.context = context;
	}

	/**
	 * Creates a filter script instance.
	 */
	@Override
	public Script createScript() {
		ServiceReference ref = context.getServiceReference(FilterManager.class.getName());
		FilterManager manager = (FilterManager) context.getService(ref);
		return new FilterScript(manager);
	}
}

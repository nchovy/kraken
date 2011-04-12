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
package org.krakenapps.syslog.impl;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.filter.FilterManager;
import org.krakenapps.syslog.SyslogScript;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class SyslogScriptFactory implements ScriptFactory {
	private BundleContext bundleContext;
	
	public SyslogScriptFactory(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	@Override
	public Script createScript() {
		ServiceReference ref = bundleContext.getServiceReference(FilterManager.class.getName());
		FilterManager filterManager = (FilterManager) bundleContext.getService(ref);
		return new SyslogScript(filterManager);
	}

}

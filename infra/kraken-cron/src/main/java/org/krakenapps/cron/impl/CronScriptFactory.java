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
package org.krakenapps.cron.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.cron.CronService;
import org.osgi.framework.BundleContext;

/**
 * The script factory class for cron service.
 * 
 * @author periphery
 * @since 1.0.0
 */
@Component(name = "cron-script-factory")
@Provides
public class CronScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "cron")
	private String alias;

	@Requires
	private CronService cron;

	private BundleContext context;

	public CronScriptFactory(BundleContext context) {
		this.context = context;
	}

	public CronScriptFactory(BundleContext context, CronService cron) {
		this.context = context;
		this.cron = cron;
	}

	@Override
	public Script createScript() {
		return new CronScript(context, cron);
	}
}

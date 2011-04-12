/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.sentry.linux.logger;

import java.util.Locale;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.log.api.AbstractLoggerFactory;
import org.krakenapps.log.api.Logger;

@Component(name = "network-usage-logger-factory")
@Provides
public class NetworkUsageLoggerFactory extends AbstractLoggerFactory {
	@Override
	public String getName() {
		return "network-usage";
	}

	@Override
	protected Logger createLogger(String namespace, String name, String description, Properties config) {
		return new NetworkUsageLogger(namespace, name, description, this);
	}

	@Override
	public String getDisplayName(Locale locale) {
		return "Network Usage Logger";
	}

	@Override
	public String getDescription(Locale locale) {
		return "Check Network Usage";
	}

}

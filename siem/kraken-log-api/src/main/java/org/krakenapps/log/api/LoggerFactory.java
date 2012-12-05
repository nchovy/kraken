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
package org.krakenapps.log.api;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.osgi.framework.BundleContext;

public interface LoggerFactory {
	/**
	 * logger factory registry calls onStart() when logger factory component is
	 * registered to OSGi service registry
	 */
	void onStart(BundleContext bc);

	/**
	 * logger factory registry calls onStop() when logger factory component is
	 * unregistering from OSGi service registry
	 */
	void onStop();

	String getFullName();

	String getNamespace();

	String getName();

	Collection<Locale> getDisplayNameLocales();

	String getDisplayName(Locale locale);

	Collection<Locale> getDescriptionLocales();

	String getDescription(Locale locale);

	Collection<LoggerConfigOption> getConfigOptions();

	Logger newLogger(String name, String description, Properties config);

	Logger newLogger(String namespace, String name, String description, Properties config);

	Logger newLogger(String namespace, String name, String description, long logCount, Date lastLogDate, Properties config);

	void deleteLogger(String name);

	void deleteLogger(String namespace, String name);

	void addListener(LoggerFactoryEventListener callback);

	void removeListener(LoggerFactoryEventListener callback);
}

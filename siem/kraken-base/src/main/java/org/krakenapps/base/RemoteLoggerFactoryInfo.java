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
package org.krakenapps.base;

import java.util.Collection;
import java.util.Locale;

import org.krakenapps.log.api.LoggerConfigOption;

public interface RemoteLoggerFactoryInfo {
	String getName();

	Collection<Locale> getDescriptionLocales();

	Collection<Locale> getDisplayNameLocales();

	String getDescription(Locale locale);

	String getDisplayName(Locale locale);

	Collection<LoggerConfigOption> getConfigOptions();
}

/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.siem.response;

import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

public interface ResponseActionManager {
	String getName();

	String getDisplayName(Locale locale);

	String getDescription(Locale locale);

	Collection<ResponseConfigOption> getConfigOptions();

	Collection<ResponseAction> getActions();

	Collection<ResponseAction> getActions(String namespace);

	ResponseAction getAction(String namespace, String name);

	ResponseAction newAction(String namespace, String name, String description, Properties config);

	void deleteAction(String namespace, String name);

	void addEventListener(ResponseActionManagerEventListener callback);

	void removeEventListener(ResponseActionManagerEventListener callback);
}

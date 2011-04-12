/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.webconsole;

import java.util.Collection;
import java.util.Locale;

public interface ProgramApi {
	Collection<Program> getPrograms();

	String getLabel(String packageId, String programId, Locale locale);

	void register(long bundleId, String packageId, String programId, String path);
	
	void localize(long bundleId, String packageId, String programId, Locale locale, String label);

	void unregister(long bundleId);
}

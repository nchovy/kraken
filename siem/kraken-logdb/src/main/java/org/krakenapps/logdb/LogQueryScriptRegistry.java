/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.logdb;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LogQueryScriptRegistry {
	Set<String> getWorkspaceNames();
	
	void createWorkspace(String name);

	void dropWorkspace(String name);

	Set<String> getScriptFactoryNames(String workspace);

	List<LogQueryScriptFactory> getScriptFactories(String workspace);
	
	LogQueryScriptFactory getScriptFactory(String workspace, String name);

	LogQueryScript newScript(String workspace, String name, Map<String, Object> params);

	void addScriptFactory(String workspace, String name, LogQueryScriptFactory factory);

	void removeScriptFactory(String workspace, String name);
}

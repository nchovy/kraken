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
package org.krakenapps.logdb.jython;

import java.util.Map;
import java.util.Set;

import org.krakenapps.logdb.LogScript;

public interface JythonLogScriptRegistry {
	Set<String> getWorkspaceNames();
	
	void dropWorkspace(String name);

	Set<String> getScriptNames(String workspace);

	String getScriptCode(String workspace, String name);

	LogScript newLogScript(String workspace, String name, Map<String, Object> params);

	void setScript(String workspace, String name, String script);

	void removeScript(String workspace, String name);
}

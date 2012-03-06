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
package org.krakenapps.httpd.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletRegistration;

public class ServletRegistrationImpl implements ServletRegistration {

	private String name;
	private String className;
	private ConcurrentMap<String, String> initParams;
	private ServletDispatcher dispatcher;

	public ServletRegistrationImpl(String name, ServletDispatcher dispatcher) {
		this.name = name;
		this.dispatcher = dispatcher;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		return initParams.putIfAbsent(name, value) == null;
	}

	@Override
	public String getInitParameter(String name) {
		return initParams.get(name);
	}

	@Override
	public Set<String> setInitParameters(Map<String, String> initParameters) {
		Set<String> conflicts = new HashSet<String>();
		for (String key : initParameters.keySet()) {
			boolean set = setInitParameter(key, initParameters.get(key));
			if (!set)
				conflicts.add(key);
		}

		return conflicts;
	}

	@Override
	public Map<String, String> getInitParameters() {
		return Collections.unmodifiableMap(initParams);
	}

	@Override
	public Set<String> addMapping(String... urlPatterns) {
		return dispatcher.addMapping(name, urlPatterns);
	}

	@Override
	public Collection<String> getMappings() {
		return dispatcher.getMappings(name);
	}

	@Override
	public String getRunAsRole() {
		return null;
	}

}

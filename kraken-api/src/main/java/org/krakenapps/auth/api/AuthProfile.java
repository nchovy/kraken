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
package org.krakenapps.auth.api;

import java.util.List;

public class AuthProfile {
	/**
	 * profile name
	 */
	private String name;

	/**
	 * at least, one provider must exists
	 */
	private List<String> providers;

	/**
	 * match all or match any
	 */
	private AuthStrategy strategy;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getProviders() {
		return providers;
	}

	public void setProviders(List<String> providers) {
		this.providers = providers;
	}

	public AuthStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(AuthStrategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public String toString() {
		return "name=" + name + ", strategy=" + strategy + ", providers=" + providers;
	}

}

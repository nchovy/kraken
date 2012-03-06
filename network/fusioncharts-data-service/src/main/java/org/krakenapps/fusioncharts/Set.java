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
package org.krakenapps.fusioncharts;

import java.util.HashMap;
import java.util.Map;

public class Set {
	private Map<String, String> attributes;
	
	public Set() {
		attributes = new HashMap<String, String>();
	}
	
	public Set setAttribute(String key, String value) {
		attributes.put(key, value);
		return this;
	}
	
	public String getAttribute(String key) {
		return attributes.get(key);
	}
	
	public java.util.Set<String> getAttributeKeys() {
		return attributes.keySet();
	}
}

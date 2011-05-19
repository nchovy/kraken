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
package org.krakenapps.radius.server;

public class RadiusConfigMetadata {
	public enum Type {
		String, Integer, Boolean;
		
		public Object parse(String s) {
			if (this == String)
				return s;
			else if (this == Integer)
				return java.lang.Integer.valueOf(s);
			else if (this == Boolean)
				return java.lang.Boolean.valueOf(s);
			else
				throw new UnsupportedOperationException();
		}
		
	}

	private Type type;
	private String name;
	private boolean isRequired;
	private Object defaultValue;

	public RadiusConfigMetadata(Type type, String name, boolean isRequired) {
		this(type, name, isRequired, null);
	}

	public RadiusConfigMetadata(Type type, String name, boolean isRequired, Object defaultValue) {
		this.type = type;
		this.name = name;
		this.isRequired = isRequired;
		this.defaultValue = defaultValue;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
}

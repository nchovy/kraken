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

public class IntegerConfigOption extends AbstractConfigOption {
	public IntegerConfigOption(String name, String displayName, String description) {
		super(name, displayName, description);
	}

	public IntegerConfigOption(String name, String displayName, String description, boolean isOptional) {
		super(name, displayName, description, isOptional);
	}

	@Override
	public Object parse(String value) {
		return Integer.valueOf(value);
	}

}

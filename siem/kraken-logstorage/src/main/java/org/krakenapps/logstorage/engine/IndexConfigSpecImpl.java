/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logstorage.engine;

import org.krakenapps.logstorage.IndexConfigSpec;

/**
 * 
 * @author xeraph
 * @since 0.9
 */
public class IndexConfigSpecImpl implements IndexConfigSpec {
	private String key;
	private boolean required;
	private String name;
	private String description;

	public IndexConfigSpecImpl(String key, boolean required, String name, String description) {
		this.key = key;
		this.required = required;
		this.name = name;
		this.description = description;
	}

	public String getKey() {
		return key;
	}

	public boolean isRequired() {
		return required;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}

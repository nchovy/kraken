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
package org.krakenapps.eventstorage.engine;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.confdb.CollectionName;

@CollectionName("table")
public class EventTableSchema {
	private int id;
	private String name;
	private Map<String, Object> metadata;

	public EventTableSchema() {
	}

	public EventTableSchema(int id, String name) {
		this.id = id;
		this.name = name;
		this.metadata = new HashMap<String, Object>();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}
}

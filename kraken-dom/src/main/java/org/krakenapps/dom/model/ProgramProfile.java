/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.dom.model;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.api.FieldOption;
import org.krakenapps.api.ReferenceKey;

public class ProgramProfile {
	@FieldOption(nullable = false)
	private String name;

	private String description;

	@ReferenceKey({ "packName", "name" })
	@CollectionTypeHint(Program.class)
	private List<Program> programs = new ArrayList<Program>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Program> getPrograms() {
		return programs;
	}

	public void setPrograms(List<Program> programs) {
		this.programs = programs;
	}
}

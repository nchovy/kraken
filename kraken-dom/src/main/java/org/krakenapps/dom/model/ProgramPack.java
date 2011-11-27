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

import java.util.Date;
import java.util.List;

import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;

@CollectionName("program-pack")
public class ProgramPack {
	@FieldOption(nullable = false, length = 60)
	private String name;

	@FieldOption(nullable = false, length = 255)
	private String dll;

	@FieldOption(length = 60)
	private String description;

	private String starter;

	private int seq;

	@FieldOption(nullable = false)
	private Date created = new Date();

	@FieldOption(nullable = false)
	private Date updated = new Date();

	@FieldOption(skip = true)
	private List<Program> programs;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDll() {
		return dll;
	}

	public void setDll(String dll) {
		this.dll = dll;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStarter() {
		return starter;
	}

	public void setStarter(String starter) {
		this.starter = starter;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public List<Program> getPrograms() {
		return programs;
	}

	public void setPrograms(List<Program> programs) {
		this.programs = programs;
	}
}

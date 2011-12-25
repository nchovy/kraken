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

import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;

@CollectionName("program")
public class Program {
	@FieldOption(name = "pack", nullable = false)
	private String pack;

	@FieldOption(nullable = false, length = 60)
	private String name;

	@FieldOption(length = 255)
	private String description;

	@FieldOption(name = "path", length = 255)
	private String path;

	private boolean visible; // in start menu
	private int seq;

	@FieldOption(nullable = false)
	private Date created = new Date();

	@FieldOption(nullable = false)
	private Date updated = new Date();

	public String getPack() {
		return pack;
	}

	public void setPack(String pack) {
		this.pack = pack;
	}

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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
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

	@Override
	public String toString() {
		return "pack=" + pack + ", name=" + name + ", description=" + description + ", path=" + path + ", visible="
				+ visible + ", seq=" + seq + ", created=" + created + ", updated=" + updated;
	}
}

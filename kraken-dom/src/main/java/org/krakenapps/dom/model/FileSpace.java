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
import java.util.UUID;

import org.krakenapps.api.FieldOption;
import org.krakenapps.api.ReferenceKey;
import org.krakenapps.confdb.CollectionName;

@CollectionName("file-space")
public class FileSpace {
	@FieldOption(nullable = false)
	private String guid = UUID.randomUUID().toString();

	@FieldOption(nullable = false)
	@ReferenceKey("loginName")
	private User owner;

	@FieldOption(nullable = false, length = 255)
	private String name;

	@FieldOption(nullable = false)
	private Date created = new Date();

	@FieldOption(nullable = false)
	private Date updated = new Date();

	@FieldOption(skip = true)
	private List<UploadedFile> files;

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public List<UploadedFile> getFiles() {
		return files;
	}

	public void setFiles(List<UploadedFile> files) {
		this.files = files;
	}
}

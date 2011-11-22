/*
 * Copyright 2010 NCHOVY
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

import java.io.File;
import java.util.Date;
import java.util.UUID;

import org.krakenapps.api.FieldOption;
import org.krakenapps.api.ReferenceKey;

public class UploadedFile {
	@FieldOption(nullable = false)
	private String guid = UUID.randomUUID().toString();

	@FieldOption(nullable = false)
	@ReferenceKey("loginName")
	private User owner;

	@ReferenceKey("guid")
	private FileSpace space;

	@FieldOption(nullable = false, length = 128)
	private String fileName;

	private long fileSize;

	@FieldOption(nullable = false)
	private String path;

	@FieldOption(nullable = false)
	private Date createDateTime = new Date();

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

	public FileSpace getSpace() {
		return space;
	}

	public void setSpace(FileSpace space) {
		this.space = space;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public File getFile() {
		return new File(path);
	}
}

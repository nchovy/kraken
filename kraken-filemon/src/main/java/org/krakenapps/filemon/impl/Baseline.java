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
 */package org.krakenapps.filemon.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class Baseline {
	private File file;
	private long fileSize;
	private long lastModified;
	private boolean hidden;
	private String hash;

	public Baseline(File file, long fileSize, long lastModified, boolean hidden, String hash) {
		this.file = file;
		this.fileSize = fileSize;
		this.lastModified = lastModified;
		this.hidden = hidden;
		this.hash = hash;
	}

	public File getFile() {
		return file;
	}

	public long getFileSize() {
		return fileSize;
	}

	public long getLastModified() {
		return lastModified;
	}

	public boolean isHidden() {
		return hidden;
	}

	public String getHash() {
		return hash;
	}

	public boolean isDeleted() {
		return !file.exists();
	}

	public boolean isModified() throws IOException {
		if (isSizeChanged())
			return true;

		if (isLastModifiedChanged())
			return true;

		if (isHiddenChanged())
			return true;

		if (isHashChanged())
			return true;

		return false;
	}

	public boolean isSizeChanged() {
		return file.length() != fileSize;
	}

	public boolean isLastModifiedChanged() {
		return file.lastModified() != lastModified;
	}

	public boolean isHiddenChanged() {
		return file.isHidden() != hidden;
	}

	public boolean isHashChanged() throws IOException {
		String s = HashUtils.getSha1(file);
		return !s.equals(hash);
	}

	public FileChange getFileChange() throws IOException {
		String currentHash = HashUtils.getSha1(file);
		long currentFileSize = file.length();
		Date currentModified = new Date(file.lastModified());

		return new FileChange(file, hash, fileSize, new Date(lastModified), currentHash,
				currentFileSize, currentModified);
	}

	@Override
	public String toString() {
		return String.format("file=%s, size=%d, last_modified=%d, hidden=%s, hash=%s", file.getAbsolutePath(),
				fileSize, lastModified, hidden, hash);
	}
}

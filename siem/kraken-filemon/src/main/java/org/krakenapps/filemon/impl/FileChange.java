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
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileChange {
	private File file;
	private String baselineHash;
	private long baselineFileSize;
	private Date baselineModified;
	private String currentHash;
	private long currentFileSize;
	private Date currentModified;

	public FileChange(File file, String baselineHash, long baselineFileSize, Date baselineModified, String currentHash,
			long currentFileSize, Date currentModified) {
		this.file = file;
		this.baselineHash = baselineHash;
		this.baselineFileSize = baselineFileSize;
		this.baselineModified = baselineModified;
		this.currentHash = currentHash;
		this.currentFileSize = currentFileSize;
		this.currentModified = currentModified;
	}

	public File getFile() {
		return file;
	}

	public String getBaselineHash() {
		return baselineHash;
	}

	public long getBaselineFileSize() {
		return baselineFileSize;
	}

	public Date getBaselineModified() {
		return baselineModified;
	}

	public String getCurrentHash() {
		return currentHash;
	}

	public long getCurrentFileSize() {
		return currentFileSize;
	}

	public Date getCurrentModified() {
		return currentModified;
	}

	@Override
	public String toString() {
		String hashChanges = baselineHash;
		if (!baselineHash.equals(currentHash))
			hashChanges += " => " + currentHash;

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String modifiedChanges = dateFormat.format(baselineModified);
		if (baselineModified != currentModified)
			modifiedChanges += " => " + dateFormat.format(currentModified);

		String sizeChanges = "" + baselineFileSize;
		if (baselineFileSize != currentFileSize)
			sizeChanges += " => " + currentFileSize;

		return String.format("%s hash: [%s], modified: [%s], size: [%s]", file.getAbsolutePath(),
				hashChanges, modifiedChanges, sizeChanges);
	}
}

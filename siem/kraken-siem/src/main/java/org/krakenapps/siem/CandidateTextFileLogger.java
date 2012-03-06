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
package org.krakenapps.siem;

import java.io.File;
import java.util.Properties;

public class CandidateTextFileLogger {
	private String name;
	private File file;
	private String fileEncoding;
	private Properties metadata = new Properties();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFileEncoding() {
		return fileEncoding;
	}

	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

	public Properties getMetadata() {
		return metadata;
	}

	public void setMetadata(Properties metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return String.format("name=%s, file=%s (%s)", name, file.getAbsolutePath(), fileEncoding);
	}

}

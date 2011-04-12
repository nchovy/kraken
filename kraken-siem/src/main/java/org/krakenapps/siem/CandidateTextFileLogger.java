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
	private String dateLocale;
	private String datePattern;
	private String parserFactoryName;
	private Properties parserOptions = new Properties();

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

	public String getDateLocale() {
		return dateLocale;
	}

	public void setDateLocale(String dateLocale) {
		this.dateLocale = dateLocale;
	}

	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	public String getParserFactoryName() {
		return parserFactoryName;
	}

	public void setParserFactoryName(String parserName) {
		this.parserFactoryName = parserName;
	}

	public Properties getParserOptions() {
		return parserOptions;
	}

	public void setParserOptions(Properties parserOptions) {
		this.parserOptions = parserOptions;
	}

	@Override
	public String toString() {
		return String.format("name=%s, file=%s (%s), parser=%s, date pattern=\"%s\" (%s)", name,
				file.getAbsolutePath(), fileEncoding, parserFactoryName, datePattern, dateLocale);
	}

}

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
 */
package org.krakenapps.filemon;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

public interface FileMonitorService {
	String getMd5(File f) throws IOException;

	String getSha1(File f) throws IOException;

	void createBaseline() throws IOException;

	void check();
	
	Date getLastTimestamp();
	
	Integer getLastFileCount();

	Collection<File> getInclusionPaths();

	void addInclusionPath(File f);

	void removeInclusionPath(File f);

	Collection<Pattern> getExclusionPatterns();

	void addExclusionPattern(String regex);

	void removeExclusionPattern(String regex);

	void addEventListener(FileMonitorEventListener callback);

	void removeEventListener(FileMonitorEventListener callback);
}

/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.logdb.sort;

import java.io.File;
import java.util.List;

class Run {
	public int id;
	public int length;
	public File indexFile;
	public File dataFile;
	public List<Object> cached;

	// skip n objects
	public int offset;

	public Run(int id, int length, File indexFile, File dataFile) {
		this(id, length, indexFile, dataFile, 0);
	}

	public Run(int id, int length, File indexFile, File dataFile, int offset) {
		this.length = length;
		this.indexFile = indexFile;
		this.dataFile = dataFile;
		this.offset = offset;
	}

	public Run(int id, List<Object> cached) {
		this.length = cached.size();
		this.cached = cached;
	}

	public void updateLength() {
		if (cached != null)
			this.length = cached.size();
	}

	@Override
	public String toString() {
		if (cached != null)
			return "Run #" + id + " cached: " + length;
		return "Run #" + id + " file: " + length;
	}

}

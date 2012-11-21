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

public class ReferenceCountedFile extends File {
	private static final long serialVersionUID = 1L;
	private int count = 0;
	
	public ReferenceCountedFile(String pathname) {
		super(pathname);
	}

	public ReferenceCountedFile share() {
		count++;
		return this;
	}

	@Override
	public boolean delete() {
		count--;
		if (count <= 0)
			return super.delete();
		return false;
	}

	@Override
	public void deleteOnExit() {
		count--;
		if (count <= 0)
			super.deleteOnExit();
	}

}

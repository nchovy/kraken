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
package org.krakenapps.confdb;

import java.util.List;
import java.util.Set;

public interface Manifest {
	int getVersion();
	
	int getId();

	void setId(int id);

	Set<String> getCollectionNames();

	int getCollectionId(String name);

	CollectionEntry getCollectionEntry(String name);

	List<ConfigEntry> getConfigEntries(String colName);

	boolean containsDoc(String colName, int docId, long rev);

	void add(CollectionEntry e);

	void remove(CollectionEntry e);

	void add(ConfigEntry e);

	void remove(ConfigEntry e);

	byte[] serialize();
	
	String toString();
}

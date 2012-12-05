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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.krakenapps.api.PrimitiveParseCallback;

public interface ConfigIterator extends Iterator<Config> {
	void setParser(ConfigParser parser);

	List<Config> getConfigs(int offset, int limit);

	Collection<Object> getDocuments();

	<T> Collection<T> getDocuments(Class<T> cls);

	<T> Collection<T> getDocuments(Class<T> cls, PrimitiveParseCallback callback);

	<T> Collection<T> getDocuments(Class<T> cls, PrimitiveParseCallback callback, int offset, int limit);

	<T> Collection<T> getObjects(ObjectBuilder<T> builder, int offset, int limit);

	int count();

	void close();
}

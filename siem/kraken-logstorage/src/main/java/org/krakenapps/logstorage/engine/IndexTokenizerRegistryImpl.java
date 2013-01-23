/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logstorage.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.logstorage.IndexTokenizer;
import org.krakenapps.logstorage.IndexTokenizerFactory;
import org.krakenapps.logstorage.IndexTokenizerRegistry;

/**
 * @since 0.9
 * @author xeraph
 */
@Component(name = "logstorage-index-tokenizer-registry")
@Provides
public class IndexTokenizerRegistryImpl implements IndexTokenizerRegistry {

	private ConcurrentHashMap<String, IndexTokenizerFactory> factories;

	public IndexTokenizerRegistryImpl() {
		factories = new ConcurrentHashMap<String, IndexTokenizerFactory>();
	}

	@Override
	public List<IndexTokenizerFactory> getFactories() {
		return new ArrayList<IndexTokenizerFactory>(factories.values());
	}

	@Override
	public IndexTokenizerFactory getFactory(String name) {
		return factories.get(name);
	}

	@Override
	public void registerFactory(IndexTokenizerFactory factory) {
		factories.put(factory.getName(), factory);
	}

	@Override
	public void unregisterFactory(IndexTokenizerFactory factory) {
		factories.remove(factory.getName(), factory);
	}

	@Override
	public IndexTokenizer newTokenizer(String name, Map<String, String> config) {
		IndexTokenizerFactory factory = factories.get(name);
		if (factory == null)
			throw new IllegalStateException("index tokenizer factory not found: " + name);

		return factory.newIndexTokenizer(config);
	}

}

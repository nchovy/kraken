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

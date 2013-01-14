package org.krakenapps.logstorage;

import java.util.List;
import java.util.Map;

public interface IndexTokenizerRegistry {
	List<IndexTokenizerFactory> getFactories();
	
	IndexTokenizerFactory getFactory(String name);

	void registerFactory(IndexTokenizerFactory factory);

	void unregisterFactory(IndexTokenizerFactory factory);

	IndexTokenizer newTokenizer(String name, Map<String, String> config);
}

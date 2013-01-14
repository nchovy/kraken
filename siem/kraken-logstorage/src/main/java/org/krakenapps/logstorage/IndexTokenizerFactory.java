package org.krakenapps.logstorage;

import java.util.Map;

public interface IndexTokenizerFactory {
	String getName();
	
	IndexTokenizer newIndexTokenizer(Map<String, String> config);
}

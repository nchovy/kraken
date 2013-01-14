package org.krakenapps.logstorage;

import java.util.Map;
import java.util.Set;

public interface IndexTokenizer {
	Set<String> tokenize(Map<String, Object> m);
}

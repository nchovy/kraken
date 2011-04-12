package org.krakenapps.log.api;

import java.util.Collection;

public interface LogParserFactoryRegistry {
	void register(LogParserFactory factory);

	void unregister(LogParserFactory factory);
	
	Collection<String> getNames();

	LogParserFactory get(String name);

	void addEventListener(LogParserFactoryRegistryEventListener callback);

	void removeEventListener(LogParserFactoryRegistryEventListener callback);
}

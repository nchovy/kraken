package org.krakenapps.log.api;

public interface LogParserFactoryRegistryEventListener {
	void factoryAdded(LogParserFactory factory);

	void factoryRemoved(LogParserFactory factory);
}

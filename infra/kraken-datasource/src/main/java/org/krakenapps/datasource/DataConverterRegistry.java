package org.krakenapps.datasource;

import java.util.Collection;

public interface DataConverterRegistry {
	Collection<DataConverter> getDataConverters();

	Collection<DataConverter> getDataConverters(String inputType, String outputType);

	DataConverter getDataConverter(String name);

	void addDataConverter(DataConverter converter);

	void removeDataConverter(String name);

	void removeDataConverter(DataConverter converter);
}

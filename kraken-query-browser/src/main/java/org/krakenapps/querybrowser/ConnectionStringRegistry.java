package org.krakenapps.querybrowser;

import java.util.Collection;
import java.util.Properties;

public interface ConnectionStringRegistry {
	Collection<String> getNames();

	Properties getConnectionString(String name);

	void setConnectionString(String name, Properties props);

	void removeConnectionString(String name);
}

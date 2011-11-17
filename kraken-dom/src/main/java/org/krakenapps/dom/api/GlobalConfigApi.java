package org.krakenapps.dom.api;

import java.util.Map;

public interface GlobalConfigApi {
	Map<String, Object> getConfigs();

	Map<String, Object> getConfigs(boolean getHidden);

	Object getConfig(String key);

	void setConfig(String key, Object value);

	void setConfig(String key, Object value, boolean isHidden);

	void unsetConfig(String key);
}

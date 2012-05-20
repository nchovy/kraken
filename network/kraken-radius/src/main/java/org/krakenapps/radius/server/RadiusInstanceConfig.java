package org.krakenapps.radius.server;

import java.util.List;
import java.util.Map;

import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;
import org.krakenapps.radius.server.RadiusConfigMetadata.Type;

@CollectionName("instances")
public class RadiusInstanceConfig {
	@FieldOption(nullable = false)
	private String name;

	@FieldOption(nullable = false)
	private String factoryName;

	private Map<String, Object> configs;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public Map<String, Object> getConfigs() {
		return configs;
	}

	public void setConfigs(Map<String, Object> configs) {
		this.configs = configs;
	}

	public void verify(List<RadiusConfigMetadata> spec) {
		for (RadiusConfigMetadata metadata : spec) {
			if (!metadata.isRequired())
				continue;

			String key = metadata.getName();
			if (!configs.containsKey(key))
				throw new IllegalStateException("[" + key + "] config not found");

			Object value = configs.get(key);
			if (value == null)
				continue;

			if (metadata.getType() == Type.String && !(value instanceof String))
				throw new IllegalStateException("[" + key + "] config should be string type");
			else if (metadata.getType() == Type.Integer && !(value instanceof Integer))
				throw new IllegalStateException("[" + key + "] config should be integer type");
			else if (metadata.getType() == Type.Boolean && !(value instanceof Boolean))
				throw new IllegalStateException("[" + key + "] config should be boolean type");
		}
	}

}

package org.krakenapps.keystore;

import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;

@CollectionName("keystores")
public class KeyStoreConfig {
	@FieldOption(nullable = false)
	private String alias;

	@FieldOption(nullable = false)
	private String type;

	@FieldOption(nullable = false)
	private String path;

	@FieldOption(nullable = true)
	private String password;

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}

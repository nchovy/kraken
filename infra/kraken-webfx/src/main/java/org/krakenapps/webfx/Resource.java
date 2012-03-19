package org.krakenapps.webfx;

import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;

@CollectionName("resources")
public class Resource {
	@FieldOption(nullable = false)
	private String name;

	@FieldOption(nullable = false)
	private String pathPrefix = "";

	public Resource() {
	}

	public Resource(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPathPrefix() {
		return pathPrefix;
	}

	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}

	@Override
	public String toString() {
		String s = name;
		if (pathPrefix != null && !pathPrefix.isEmpty())
			s += ", pathPrefix=" + pathPrefix;
		return s;
	}

}

package org.krakenapps.webfx;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.confdb.CollectionName;

@CollectionName("webapps")
public class WebApplication {
	/**
	 * http context name. one application to one http context.
	 */
	private String context;

	/**
	 * root directory path
	 */
	private String rootPath;

	/**
	 * resources for routing
	 */
	@CollectionTypeHint(Resource.class)
	private List<Resource> resources = new ArrayList<Resource>();

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	@Override
	public String toString() {
		return "context=" + context + ", rootPath=" + rootPath + ", resources=" + resources;
	}

}

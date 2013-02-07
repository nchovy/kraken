/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

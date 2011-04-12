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
package org.krakenapps.webconsole.impl;

import java.io.InputStream;
import java.net.URL;

import org.krakenapps.webconsole.HttpRequest;
import org.krakenapps.webconsole.StaticResourceContext;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleResourceContext implements StaticResourceContext {
	private final Logger logger = LoggerFactory.getLogger(BundleResourceContext.class.getName());
	private Bundle bundle;
	private String basePath;

	public BundleResourceContext(Bundle bundle, String basePath) {
		this.bundle = bundle;
		this.basePath = basePath;
	}

	@Override
	public InputStream open(HttpRequest req) {
		try {
			URL url = bundle.getEntry(basePath + "/" + req.getPath());
			return url.openStream();
		} catch (Exception e) {
			logger.trace("kraken webconsole: cannot open bundle [{}] resource [{}]", bundle.getBundleId(),
					req.getPath());
			return null;
		}
	}

	@Override
	public String toString() {
		return bundle.getEntry("/").toString();
	}

}

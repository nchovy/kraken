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
package org.krakenapps.isc.api.impl;

import java.util.Dictionary;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.krakenapps.isc.api.IscClientConfig;
import org.krakenapps.isc.api.IscClientEventListener;

public class ApiKeyHandler extends PrimitiveHandler implements IscClientEventListener {

	private IscClientConfig config;

	@SuppressWarnings("rawtypes")
	@Override
	public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {
	}

	@Override
	public void start() {
		setValidity(false);
		if (config != null)
			validate();
	}

	@Override
	public void stop() {
		if (config != null)
			config.removeEventListener(this);
	}

	public void validate() {
		config.addEventListener(this);
		setValidity(config.getApiKey() != null);
	}

	@Override
	public void onConfigure(String apiKey) {
		// TODO: key validation check
		setValidity(apiKey != null);
	}

	public void setIscClientConfig(IscClientConfig config) {
		this.config = config;
		validate();
	}
}

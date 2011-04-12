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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.webconsole.HttpRequest;

public class WebConsoleHttpRequest implements HttpRequest {
	private String path;
	private Map<String, String> headers;
	private Map<String, String> parameters;
	
	public WebConsoleHttpRequest() {
		headers = new HashMap<String, String>();
		parameters = new HashMap<String, String>();
	}

	@Override
	public Collection<String> getHeaderNames() {
		return new ArrayList<String>(headers.keySet());
	}

	@Override
	public String getHeader(String header) {
		return headers.get(header);
	}
	
	public void putHeader(String header, String value) {
		headers.put(header, value);
	}

	@Override
	public Collection<String> getParameterNames() {
		return new ArrayList<String>(parameters.keySet());
	}

	@Override
	public String getParameter(String key) {
		return parameters.get(key);
	}
	
	public void putParameter(String key, String value) {
		parameters.put(key, value);
	}

	@Override
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
}

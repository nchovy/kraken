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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.krakenapps.isc.api.IscClient;
import org.krakenapps.isc.api.IscClientConfig;
import org.krakenapps.xmlrpc.XmlRpcClient;
import org.krakenapps.xmlrpc.XmlRpcFaultException;

public class IscClientService implements IscClient {
	private static final String SITE_PATH = "http://nchovy.kr/xmlrpc";
	private final URL url;

	private IscClientConfig config;

	public IscClientService() throws MalformedURLException {
		this.url = new URL(SITE_PATH);
	}

	@Override
	public Object call(String method, Object... args) throws XmlRpcFaultException, IOException {
		Object[] added = new Object[args.length + 1];
		added[0] = config.getApiKey();
		for (int i = 0; i < args.length; i++)
			added[i + 1] = args[i];

		return XmlRpcClient.call(url, method, added);
	}
}

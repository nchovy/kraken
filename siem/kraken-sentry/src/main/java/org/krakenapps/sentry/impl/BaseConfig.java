/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.sentry.impl;

import java.net.InetSocketAddress;

import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;
import org.krakenapps.sentry.Base;

@CollectionName("bases")
public class BaseConfig implements Base {
	@FieldOption(nullable = false)
	private String name;

	@FieldOption(nullable = false)
	private String ip;

	@FieldOption(nullable = false)
	private int port;

	@FieldOption(nullable = false)
	private String keyAlias;

	@FieldOption(nullable = false)
	private String trustAlias;

	public BaseConfig() {
	}

	public BaseConfig(String name, InetSocketAddress address, String keyAlias, String trustAlias) {
		this.name = name;
		this.ip = address.getAddress().getHostAddress();
		this.port = address.getPort();
		this.keyAlias = keyAlias;
		this.trustAlias = trustAlias;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public InetSocketAddress getAddress() {
		return new InetSocketAddress(ip, port);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String getKeyAlias() {
		return keyAlias;
	}

	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
	}

	@Override
	public String getTrustAlias() {
		return trustAlias;
	}

	public void setTrustAlias(String trustAlias) {
		this.trustAlias = trustAlias;
	}

	@Override
	public String toString() {
		return String.format("name=%s, address=%s, key=%s, ca=%s", name, getAddress(), keyAlias, trustAlias);
	}

}

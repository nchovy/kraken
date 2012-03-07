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

import org.krakenapps.sentry.Base;

public class BaseImpl implements Base {
	private String name;
	private InetSocketAddress address;
	private String keyAlias;
	private String trustAlias;

	public BaseImpl(String name, InetSocketAddress address, String keyAlias, String trustAlias) {
		this.name = name;
		this.address = address;
		this.keyAlias = keyAlias;
		this.trustAlias = trustAlias;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public InetSocketAddress getAddress() {
		return address;
	}

	@Override
	public String getKeyAlias() {
		return keyAlias;
	}

	@Override
	public String getTrustAlias() {
		return trustAlias;
	}

	@Override
	public String toString() {
		return String.format("name=%s, address=%s, key=%s, ca=%s", name, address, keyAlias, trustAlias);
	}

}

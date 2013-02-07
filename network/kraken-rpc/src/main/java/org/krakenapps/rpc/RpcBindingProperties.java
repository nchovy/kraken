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
package org.krakenapps.rpc;

import org.krakenapps.confdb.CollectionName;

@CollectionName("bindings")
public class RpcBindingProperties {
	private String addr;
	private int port;
	private String keyAlias;
	private String trustAlias;

	public RpcBindingProperties() {
	}

	public RpcBindingProperties(int port) {
		this("0.0.0.0", port);
	}

	public RpcBindingProperties(String addr, int port) {
		this(addr, port, null, null);
	}

	public RpcBindingProperties(String host, int port, String keyAlias, String trustAlias) {
		this.addr = host;
		this.port = port;
		this.keyAlias = keyAlias;
		this.trustAlias = trustAlias;
	}

	public String getHost() {
		return addr;
	}

	public void setHost(String host) {
		this.addr = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getKeyAlias() {
		return keyAlias;
	}

	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
	}

	public String getTrustAlias() {
		return trustAlias;
	}

	public void setTrustAlias(String trustAlias) {
		this.trustAlias = trustAlias;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addr == null) ? 0 : addr.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RpcBindingProperties other = (RpcBindingProperties) obj;
		if (addr == null) {
			if (other.addr != null)
				return false;
		} else if (!addr.equals(other.addr))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "addr=" + addr + ", port=" + port + ", key=" + keyAlias + ", trust=" + trustAlias;
	}

}

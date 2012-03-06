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
package org.krakenapps.dhcp.model;

import java.net.InetAddress;

public class DhcpIpGroup {
	private String name;
	private String description;
	private InetAddress from;
	private InetAddress to;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public InetAddress getFrom() {
		return from;
	}

	public void setFrom(InetAddress from) {
		this.from = from;
	}

	public InetAddress getTo() {
		return to;
	}

	public void setTo(InetAddress to) {
		this.to = to;
	}

	@Override
	public String toString() {
		return String.format("name=%s, range=[%s~%s], description=%s", name, from.getHostAddress(), to
				.getHostAddress(), description);
	}

}

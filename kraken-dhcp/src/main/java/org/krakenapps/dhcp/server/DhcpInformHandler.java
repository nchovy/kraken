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
package org.krakenapps.dhcp.server;

import java.io.IOException;
import java.util.List;

import org.krakenapps.dhcp.DhcpMessage;
import org.krakenapps.dhcp.DhcpServer;
import org.krakenapps.dhcp.model.DhcpIpGroup;
import org.krakenapps.dhcp.model.DhcpOptionConfig;

class DhcpInformHandler {
	private DhcpServer server;
	private DhcpServerContext c;

	public DhcpInformHandler(DhcpServer server, DhcpServerContext c) {
		this.server = server;
		this.c = c;
	}

	public void handle(DhcpMessage msg) throws IOException {
		// RFC allow only ACK
		DhcpIpGroup group = server.getIpGroup(msg.getClientAddress());
		if (group == null)
			return;

		List<DhcpOptionConfig> configs = server.getGroupOptions(group.getName());
		c.send(msg.getClientAddress(), DhcpMessageBuilder.newAck(msg, configs, DhcpServerContext.ZERO_ADDRESS));
	}
}

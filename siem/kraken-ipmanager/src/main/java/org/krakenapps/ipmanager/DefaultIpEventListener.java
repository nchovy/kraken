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
package org.krakenapps.ipmanager;

import java.net.InetAddress;

import org.krakenapps.ipmanager.model.Agent;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;

public abstract class DefaultIpEventListener implements IpEventListener {

	@Override
	public void onNewIpDetected(Agent agent, InetAddress ip, MacAddress mac) {
	}

	@Override
	public void onNewMacDetected(Agent agent, InetAddress ip, MacAddress mac) {
	}

	@Override
	public void onIpChanged(Agent agent, InetAddress ip1, InetAddress ip2, MacAddress mac) {
	}

	@Override
	public void onMacChanged(Agent agent, InetAddress ip, MacAddress oldMac, MacAddress newMac) {
	}

	@Override
	public void onIpConflict(Agent agent, InetAddress ip, MacAddress originalMac, MacAddress conflictMac) {
	}
}

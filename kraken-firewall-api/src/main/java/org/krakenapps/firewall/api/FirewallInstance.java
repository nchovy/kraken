/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.firewall.api;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Properties;

public interface FirewallInstance {
	FirewallInstanceManager getInstanceManager();
	
	String getName();

	Properties getConfig();

	Collection<InetAddress> getBlockedSources();

	void blockSourceIp(InetAddress ip);

	void unblockSourceIp(InetAddress ip);
}

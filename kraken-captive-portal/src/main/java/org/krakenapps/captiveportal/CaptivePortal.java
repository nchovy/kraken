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
package org.krakenapps.captiveportal;

import java.net.InetAddress;
import java.util.Collection;

public interface CaptivePortal {
	String getPcapDeviceName();
	
	void setPcapDeviceName(String name);
	
	int getPoisonInterval();

	void setPoisonInterval(int milliseconds);

	InetAddress getGatewayAddress();

	void setGatewayAddress(InetAddress address);

	boolean getMirroringMode();

	void setMirroringMode(boolean enabled);

	Collection<InetAddress> getQuarantinedHosts();

	void quarantineHost(InetAddress address);

	void unquarantineHost(InetAddress address);
}

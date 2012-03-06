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

import org.krakenapps.dhcp.DhcpMessage;
import org.krakenapps.dhcp.DhcpOption;
import org.krakenapps.dhcp.DhcpOptionCode;
import org.krakenapps.dhcp.options.ParameterRequestListOption;

public class DhcpOptions {
	private DhcpOptions() {
	}

	public static DhcpMessage.Type getDhcpMessageType(DhcpMessage msg) {
		DhcpOption option = msg.getOption(DhcpOptionCode.DhcpMessageType.code());
		if (option != null)
			return DhcpMessage.Type.from(option.getValue()[0]);

		return null;
	}

	public static String getDomainName(DhcpMessage msg) {
		DhcpOption option = msg.getOption(DhcpOptionCode.DomainName.code());
		if (option != null)
			return new String(option.getValue());

		return null;
	}

	public static String getHostName(DhcpMessage msg) {
		DhcpOption option = msg.getOption(DhcpOptionCode.HostName.code());
		if (option != null)
			return new String(option.getValue());

		return null;
	}

	public static String getFingerprint(DhcpMessage msg) {
		for (DhcpOption option : msg.getOptions())
			if (option instanceof ParameterRequestListOption)
				return ((ParameterRequestListOption) option).getFingerprint();

		return null;
	}

}

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
package org.krakenapps.pcap.decoder.dhcp.options;

import java.util.ArrayList;
import java.util.List;

public class ParameterRequestListOption extends RawDhcpOption {
	private List<Integer> params = new ArrayList<Integer>();

	public ParameterRequestListOption(byte type, int length, byte[] value) {
		super(type, length, value);
		
		for (int i = 0; i < value.length; i++) {
			params.add(value[i] & 0xFF);
		}
	}

	public List<Integer> getParams() {
		return params;
	}
	
	public String getFingerprint() {
		// options can be used for device fingerprinting
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (Integer param : params) {
			if (i != 0)
				sb.append(",");
			
			sb.append(param);
			i++;
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "Parameter Requets List: " + getFingerprint();
	}

}

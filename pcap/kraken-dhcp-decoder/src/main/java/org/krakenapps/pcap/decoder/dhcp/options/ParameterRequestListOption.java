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

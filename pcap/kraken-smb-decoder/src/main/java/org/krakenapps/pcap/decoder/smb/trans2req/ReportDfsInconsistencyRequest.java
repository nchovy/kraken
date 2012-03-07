package org.krakenapps.pcap.decoder.smb.trans2req;

import org.krakenapps.pcap.decoder.smb.TransData;

public class ReportDfsInconsistencyRequest implements TransData{
	@Override
	public String toString(){
		return String.format("Trans2 Second Level : Report Dfs Inconsistency Request\n" +
				"there is no implementation");
	}
}

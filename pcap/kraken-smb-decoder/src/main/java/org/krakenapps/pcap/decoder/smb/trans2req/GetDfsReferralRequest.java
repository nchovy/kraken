package org.krakenapps.pcap.decoder.smb.trans2req;

import org.krakenapps.pcap.decoder.smb.TransData;

public class GetDfsReferralRequest implements TransData{
	short subcommand;
	byte []referralRequest; // this see MS-DFSC 2.2.2
	public short getSubcommand() {
		return subcommand;
	}
	public void setSubcommand(short subcommand) {
		this.subcommand = subcommand;
	}
	public byte[] getReferralRequest() {
		return referralRequest;
	}
	public void setReferralRequest(byte[] referralRequest) {
		this.referralRequest = referralRequest;
	}
	@Override
	public String toString(){
		return String.format("Trans2 Second Level : Get Dfs Referral Request\n" +
				"subCommand = 0x%s\n",
				Integer.toHexString(this.subcommand));
	}
}

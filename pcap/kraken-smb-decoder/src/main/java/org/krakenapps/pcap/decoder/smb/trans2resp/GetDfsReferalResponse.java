package org.krakenapps.pcap.decoder.smb.trans2resp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class GetDfsReferalResponse implements TransData{
	short subcommand;
	byte []referralReseponse;
	public short getSubcommand() {
		return subcommand;
	}
	public void setSubcommand(short subcommand) {
		this.subcommand = subcommand;
	}
	public byte[] getReferralReseponse() {
		return referralReseponse;
	}
	public void setReferralReseponse(byte[] referralReseponse) {
		this.referralReseponse = referralReseponse;
	}
	@Override
	public String toString(){
		return String.format("Trans2 Seconde Level : Get Dfs Rereral Response\n");
	}
}

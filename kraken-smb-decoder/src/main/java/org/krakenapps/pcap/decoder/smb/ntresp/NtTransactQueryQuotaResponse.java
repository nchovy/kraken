package org.krakenapps.pcap.decoder.smb.ntresp;

public class NtTransactQueryQuotaResponse {

	// quotaInformation specified in MS-FSCC 2.4.33.
	// trans parameters
	int dataLength;
	// trans data
	byte []quotaInformation;
	
	public int getDataLength() {
		return dataLength;
	}
	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}
	public byte[] getQuotaInformation() {
		return quotaInformation;
	}
	public void setQuotaInformation(byte[] quotaInformation) {
		this.quotaInformation = quotaInformation;
	}
	@Override
	public String toString(){
		return String.format("Second Level : Nt Transact Query Quota Response\n"+
				"dataLength = 0x%s , quotaInformation = %s\n",
				Integer.toHexString(this.dataLength) , this.quotaInformation.toString());
	}
}

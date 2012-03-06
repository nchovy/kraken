package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0xDA
public class WriteBulkDataRequest implements SmbData{
// return STATUS_NOT_IMPLEMENTED
	boolean malformed = false;
	@Override
	public boolean isMalformed() {
		// TODO Auto-generated method stub
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	@Override
	public String toString(){
		return String.format("First Level : Write Bulk Data Request\n" +
				"isMalformed = %s\n",
				this.malformed);
	}
}

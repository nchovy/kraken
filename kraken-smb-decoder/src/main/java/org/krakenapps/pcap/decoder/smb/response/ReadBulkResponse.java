package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0xD8
public class ReadBulkResponse implements SmbData{

	boolean malformed = false;
	//return STATUS_NOT_IMPLEMENTED;
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
		return String.format("First Level : Read Bulk Response \n" +
				"isMalFormed = %s\n",
				this.malformed);
	}
}

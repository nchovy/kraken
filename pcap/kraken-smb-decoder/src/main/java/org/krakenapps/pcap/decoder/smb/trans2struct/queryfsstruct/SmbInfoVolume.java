package org.krakenapps.pcap.decoder.smb.trans2struct.queryfsstruct;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbInfoVolume implements TransStruct{

	int ulVolSerualNbr;
	byte cCharCount;
	String volumeLabel;
	public int getUlVolSerualNbr() {
		return ulVolSerualNbr;
	}
	public void setUlVolSerualNbr(int ulVolSerualNbr) {
		this.ulVolSerualNbr = ulVolSerualNbr;
	}
	public byte getcCharCount() {
		return cCharCount;
	}
	public void setcCharCount(byte cCharCount) {
		this.cCharCount = cCharCount;
	}
	public String getVolumeLabel() {
		return volumeLabel;
	}
	public void setVolumeLabel(String volumeLabel) {
		this.volumeLabel = volumeLabel;
	}
	public TransStruct parse(Buffer b , SmbSession session){
		ulVolSerualNbr = ByteOrderConverter.swap(b.getInt());
		cCharCount = b.get();
		volumeLabel = NetBiosNameCodec.readSmbUnicodeName(b, cCharCount);
		return this;
	}
	@Override
	public String toString(){
		return String.format("Third Level Structure : Smb Info Volume\n" +
				"ulVolSecualNbr = 0x%s , cCharCount= 0x%s" +
				"volumeLabel = %s\n",
				Integer.toHexString(this.ulVolSerualNbr), Integer.toHexString(this.cCharCount) , 
				this.volumeLabel);
	}
}

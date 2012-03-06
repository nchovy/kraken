package org.krakenapps.pcap.decoder.smb.trans2struct.queryfsstruct;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransStruct;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbQueryFsSizeInfo implements TransStruct{

	long totalAllocationUnits;
	long totalFreeAllocationUnits;
	int sectorsPerAllocationUnit;
	int bytesPerSector;
	public long getTotalAllocationUnits() {
		return totalAllocationUnits;
	}
	public void setTotalAllocationUnits(long totalAllocationUnits) {
		this.totalAllocationUnits = totalAllocationUnits;
	}
	public long getTotalFreeAllocationUnits() {
		return totalFreeAllocationUnits;
	}
	public void setTotalFreeAllocationUnits(long totalFreeAllocationUnits) {
		this.totalFreeAllocationUnits = totalFreeAllocationUnits;
	}
	public int getSectorsPerAllocationUnit() {
		return sectorsPerAllocationUnit;
	}
	public void setSectorsPerAllocationUnit(int sectorsPerAllocationUnit) {
		this.sectorsPerAllocationUnit = sectorsPerAllocationUnit;
	}
	public int getBytesPerSector() {
		return bytesPerSector;
	}
	public void setBytesPerSector(int bytesPerSector) {
		this.bytesPerSector = bytesPerSector;
	}
	public TransStruct parse(Buffer b , SmbSession session){
		totalAllocationUnits = ByteOrderConverter.swap(b.getLong());
		totalFreeAllocationUnits = ByteOrderConverter.swap(b.getLong());
		sectorsPerAllocationUnit = ByteOrderConverter.swap(b.getInt());
		bytesPerSector = ByteOrderConverter.swap(b.getInt());
		return this;
	}
	@Override
	public String toString(){
		return String.format("Third Level Structure : Smb Info Fs Size Info\n" +
				"totalAllocationUnits = 0x%s , totalFreeAllocationUnits = 0x%s , sectorsPerAllocationUnit = 0x%s\n" +
				"bytesPerSector = 0x%s\n",
				Long.toHexString(this.totalAllocationUnits) , Long.toHexString(this.totalFreeAllocationUnits) , Integer.toHexString(this.sectorsPerAllocationUnit),
				Integer.toHexString(this.bytesPerSector));
	}
}

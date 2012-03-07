package org.krakenapps.pcap.decoder.netbios.rr;

import org.krakenapps.pcap.decoder.netbios.DatagramData;
import org.krakenapps.pcap.util.Buffer;

public class DatagramErrorData implements DatagramData {

	public final static byte DestinationNameNotPresent = (byte) 0x82;
	public final static byte InvalidSourceNameFormat = (byte) 0x83;
	public final static byte InvalidDestinationNameFormat = (byte) 0x84;
	private byte errorCode;

	private DatagramErrorData() {

	}

	public byte getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(byte errorCode) {
		this.errorCode = errorCode;
	}

	public static DatagramErrorData parse(Buffer b) {
		DatagramErrorData data = new DatagramErrorData();
		data.setErrorCode(b.get());
		return data;
	}

	@Override
	public String toString() {
		return String.format("DatagramErrorData\n"+"ErrorCode=0x%s\n", Integer.toHexString(errorCode));
	}
}

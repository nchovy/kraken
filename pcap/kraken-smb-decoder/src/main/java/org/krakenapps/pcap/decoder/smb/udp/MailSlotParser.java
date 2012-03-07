package org.krakenapps.pcap.decoder.smb.udp;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class MailSlotParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer, Buffer parameterBuffer,
			Buffer DataBuffer){
		TransData data = new MailSlot();
		((MailSlot)data).setMailslotCode(ByteOrderConverter.swap(setupBuffer.getShort()));
		((MailSlot)data).setPriority(ByteOrderConverter.swap(setupBuffer.getShort()));
		((MailSlot)data).setCls(ByteOrderConverter.swap(setupBuffer.getShort()));
		return data;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer, Buffer parameterBuffer,
			Buffer DataBuffer, SmbSession session) {
		return null;
	}

	@Override
	public String toString() {
		return "MailSlotParser [toString()=" + super.toString() + "]";
	}


}

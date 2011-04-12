package org.krakenapps.pcap.decoder.smb.udp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class MailSlot implements TransData {

	// setup section
	short mailslotCode;
	short priority;
	short cls;// class

	// setup section

	public short getMailslotCode() {
		return mailslotCode;
	}

	public void setMailslotCode(short mailslotCode) {
		this.mailslotCode = mailslotCode;
	}

	public short getPriority() {
		return priority;
	}

	public void setPriority(short priority) {
		this.priority = priority;
	}

	public short getCls() {
		return cls;
	}

	public void setCls(short cls) {
		this.cls = cls;
	}

	@Override
	public String toString() {
		return "MailSlot [mailslotCode=" + mailslotCode + ", priority="
				+ priority + ", cls=" + cls + ", toString()="
				+ super.toString() + "]";
	}
}

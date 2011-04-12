package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.decoder.rpce.rr.PContDefResult;
import org.krakenapps.pcap.decoder.rpce.rr.PProviderReason;
import org.krakenapps.pcap.util.Buffer;

public class PResult {

	private PContDefResult result; // enum
	private PProviderReason reason;// enum
	private PSyntaxId transferSyntax;

	public PResult() {
		transferSyntax = new PSyntaxId();
	}

	public void parse(Buffer b) {
		result = PContDefResult.parse(b.get() & 0xff);
		reason = PProviderReason.parse(b.get() & 0xff);
		transferSyntax.parse(b);
	}

	public PContDefResult getResult() {
		return result;
	}

	public void setResult(PContDefResult result) {
		this.result = result;
	}

	public PProviderReason getReason() {
		return reason;
	}

	public void setReason(PProviderReason reason) {
		this.reason = reason;
	}

	public PSyntaxId getTransferSyntax() {
		return transferSyntax;
	}

	public void setTransferSyntax(PSyntaxId transferSyntax) {
		this.transferSyntax = transferSyntax;
	}

}

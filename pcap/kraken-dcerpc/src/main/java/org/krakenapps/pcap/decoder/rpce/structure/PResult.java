/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

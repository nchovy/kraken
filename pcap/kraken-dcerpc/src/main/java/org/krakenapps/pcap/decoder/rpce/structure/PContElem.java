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

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class PContElem {

	private short pContID;
	private byte nTransferSyn;
	private byte reserved;
	private PSyntaxId abstractSyntax;
	private PSyntaxId []transferSyntaxes; // n_transfer_syn = size;
	
	public void parse(Buffer b){
		pContID = ByteOrderConverter.swap(b.getShort());
		nTransferSyn = b.get();
		reserved = b.get();
		abstractSyntax = new PSyntaxId();
		//System.out.println("pContID = " + pContID);
		//System.out.println("nTransferSyn = " + nTransferSyn);
		//System.out.println("reserved = " + abstractSyntax );
		abstractSyntax.parse(b);
		transferSyntaxes = new PSyntaxId[nTransferSyn];
		for(int i=0; i<nTransferSyn;i++){
			transferSyntaxes[i] = new PSyntaxId();
			transferSyntaxes[i].parse(b);
		}
	}

	public short getpContID() {
		return pContID;
	}

	public void setpContID(short pContID) {
		this.pContID = pContID;
	}

	public byte getnTransferSyn() {
		return nTransferSyn;
	}

	public void setnTransferSyn(byte nTransferSyn) {
		this.nTransferSyn = nTransferSyn;
	}

	public byte getReserved() {
		return reserved;
	}

	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}

	public PSyntaxId getAbstractSyntax() {
		return abstractSyntax;
	}

	public void setAbstractSyntax(PSyntaxId abstractSyntax) {
		this.abstractSyntax = abstractSyntax;
	}

	public PSyntaxId[] getTransferSyntaxes() {
		return transferSyntaxes;
	}

	public void setTransferSyntaxes(PSyntaxId[] transferSyntaxes) {
		this.transferSyntaxes = transferSyntaxes;
	}
	
}

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

public class PContList {

	private byte nContextElem;
	private byte reserved;
	private short reserved2;
	private PContElem []pContElems;// nContext
	
	public void parse(Buffer b){
		nContextElem = b.get();
		reserved = b.get();
		reserved2 = ByteOrderConverter.swap(b.getShort());
		pContElems = new PContElem[nContextElem];
		//System.out.println("nContextElem = " + nContextElem);
		//System.out.println("reserved = " + reserved);
		//System.out.println("reserved2 = " + reserved2);
		for(int i=0;i<nContextElem;i++){
			pContElems[i] = new PContElem();
			pContElems[i].parse(b);
		}
		
	}
	
	
	public byte getnContextElem() {
		return nContextElem;
	}
	public void setnContextElem(byte nContextElem) {
		this.nContextElem = nContextElem;
	}
	public byte getReserved() {
		return reserved;
	}
	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}
	public short getReserved2() {
		return reserved2;
	}
	public void setReserved2(short reserved2) {
		this.reserved2 = reserved2;
	}
	public PContElem[] getpContElems() {
		return pContElems;
	}
	public void setpContElems(PContElem[] pContElems) {
		this.pContElems = pContElems;
	}
	
}

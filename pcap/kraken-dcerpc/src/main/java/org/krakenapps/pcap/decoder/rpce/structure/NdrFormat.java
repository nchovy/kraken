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

public class NdrFormat {

	private byte intRep;
	private byte charRep;
	private byte floatRep;
	private byte reserved;
	public void parse(Buffer b){
		intRep = b.get();
		charRep = b.get();
		floatRep = b.get();
		reserved = b.get();
	}
	public byte getIntRep() {
		return intRep;
	}
	public void setIntRep(byte intRep) {
		this.intRep = intRep;
	}
	public byte getCharRep() {
		return charRep;
	}
	public void setCharRep(byte charRep) {
		this.charRep = charRep;
	}
	public byte getFloatRep() {
		return floatRep;
	}
	public void setFloatRep(byte floatRep) {
		this.floatRep = floatRep;
	}
	public byte getReserved() {
		return reserved;
	}
	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}
}

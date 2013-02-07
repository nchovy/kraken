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

public class RpcrtOptionalData {

	private byte rpcVers;
	private byte rpcVersMinors;
	private byte[] reserved; // 2bytes
	private byte[] packedDrep; // 4byte
	private int regectStatus;
	private byte[] reserved2; // 4byte

	public RpcrtOptionalData() {
		reserved = new byte[2];
		packedDrep = new byte[4];
		reserved2 = new byte[4];
	}

	public void parse(Buffer b) {
		rpcVers = b.get();
		rpcVersMinors = b.get();
		b.gets(reserved);
		b.gets(packedDrep);
		regectStatus = ByteOrderConverter.swap(b.getInt());
		b.gets(reserved2);
	}

	public byte getRpcVers() {
		return rpcVers;
	}

	public void setRpcVers(byte rpcVers) {
		this.rpcVers = rpcVers;
	}

	public byte getRpcVersMinors() {
		return rpcVersMinors;
	}

	public void setRpcVersMinors(byte rpcVersMinors) {
		this.rpcVersMinors = rpcVersMinors;
	}

	public byte[] getReserved() {
		return reserved;
	}

	public void setReserved(byte[] reserved) {
		this.reserved = reserved;
	}

	public byte[] getPackedDrep() {
		return packedDrep;
	}

	public void setPackedDrep(byte[] packedDrep) {
		this.packedDrep = packedDrep;
	}

	public int getRegectStatus() {
		return regectStatus;
	}

	public void setRegectStatus(int regectStatus) {
		this.regectStatus = regectStatus;
	}

	public byte[] getReserved2() {
		return reserved2;
	}

	public void setReserved2(byte[] reserved2) {
		this.reserved2 = reserved2;
	}

}

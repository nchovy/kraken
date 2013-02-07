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

public class RpcIfId {

	private int uid;
	private int versMajor;
	private int versMinor;
	public void parse(Buffer b){
		uid = ByteOrderConverter.swap(b.getInt());
		versMajor = ByteOrderConverter.swap(b.getInt());
		versMinor = ByteOrderConverter.swap(b.getInt());
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public int getVersMajor() {
		return versMajor;
	}
	public void setVersMajor(int versMajor) {
		this.versMajor = versMajor;
	}
	public int getVersMinor() {
		return versMinor;
	}
	public void setVersMinor(int versMinor) {
		this.versMinor = versMinor;
	}
	
}

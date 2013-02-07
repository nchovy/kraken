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

import java.util.Arrays;

import org.krakenapps.pcap.util.Buffer;

public class Uuid {

	byte []buff;
	public Uuid(){
		buff = new byte[16];
	}
	public void parse(Buffer b){
		b.gets(buff);
		return;
	}
	@Override
	public String toString() {
		return "Uuid [buff=" + Arrays.toString(buff) + "]";
	}
	public byte[] getBuff() {
		return buff;
	}
	public void setBuff(byte[] buff) {
		this.buff = buff;
	}
	
}

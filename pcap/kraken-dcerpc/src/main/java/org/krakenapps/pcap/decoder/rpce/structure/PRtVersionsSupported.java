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


public class PRtVersionsSupported {

	byte nProtocols;
	Version []pProtocols;// size = n_protocols;
	public void parse(Buffer b){
		nProtocols = b.get();
		pProtocols = new Version[nProtocols];
		for(int i=0;i<nProtocols;i++){
			pProtocols[i] = new Version();
			pProtocols[i].parse(b);
		}
	}
	public byte getnProtocols() {
		return nProtocols;
	}
	public void setnProtocols(byte nProtocols) {
		this.nProtocols = nProtocols;
	}
	public Version[] getpProtocols() {
		return pProtocols;
	}
	public void setpProtocols(Version[] pProtocols) {
		this.pProtocols = pProtocols;
	}

	
}

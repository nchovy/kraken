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

public class NdrContexthandle {

	private int contextHandleAttribues;
	private Uuid contextHandleUuid;
	public void parse(Buffer b){
		contextHandleAttribues = ByteOrderConverter.swap(b.getInt());
		contextHandleUuid.parse(b);
	}
	public int getContextHandleAttribues() {
		return contextHandleAttribues;
	}
	public void setContextHandleAttribues(int contextHandleAttribues) {
		this.contextHandleAttribues = contextHandleAttribues;
	}
	public Uuid getContextHandleUuid() {
		return contextHandleUuid;
	}
	public void setContextHandleUuid(Uuid contextHandleUuid) {
		this.contextHandleUuid = contextHandleUuid;
	}
	
}

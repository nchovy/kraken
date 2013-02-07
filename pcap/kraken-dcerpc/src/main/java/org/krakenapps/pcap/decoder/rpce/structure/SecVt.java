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

public class SecVt {

	public final short SEC_VT_COMMAND_BITMASK1 = 0x0001;
	public final short SEC_VT_COMMMND_PCONTEXT = 0x0002;
	public final short SEC_VT_COMMAND_HEADER2 = 0x0003;
	public final short SEC_VT_COMMAND_END = 0x4000;
	public final short SEC_VT_MUST_PROCESS_COMMAND = (short)0x8000;
	
	private short command;
	private short length; // must be multiple 4
	public void parse(Buffer b){
		command = ByteOrderConverter.swap(b.getShort());
		length = ByteOrderConverter.swap(b.getShort());
	}
	public short getCommand() {
		return command;
	}
	public void setCommand(short command) {
		this.command = command;
	}
	public short getLength() {
		return length;
	}
	public void setLength(short length) {
		this.length = length;
	}
}

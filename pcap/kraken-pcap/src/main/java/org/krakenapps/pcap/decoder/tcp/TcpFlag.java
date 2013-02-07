/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.pcap.decoder.tcp;

public class TcpFlag {
	public static final int FIN = 0x1;
	public static final int SYN = 0x2;
	public static final int RST = 0x4;
	public static final int PSH = 0x8;
	public static final int ACK = 0x10;
	public static final int URG = 0x20;
	
	private TcpFlag() {
	}
}
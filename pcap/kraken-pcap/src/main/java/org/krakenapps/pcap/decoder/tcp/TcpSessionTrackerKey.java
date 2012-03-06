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

import java.net.InetAddress;

/**
 * @author mindori
 */
public class TcpSessionTrackerKey {
	public static int activeHashCode(String token) {
		String[] portCommand = token.split(",");
		int portNum = (Integer.parseInt(portCommand[4]) * 256) + Integer.parseInt(portCommand[5].replaceAll("\r\n", ""));
		return Integer.parseInt(portCommand[0]) ^ Integer.parseInt(portCommand[1]) ^ Integer.parseInt(portCommand[2]) ^ Integer.parseInt(portCommand[3]) ^ portNum;
	}
	
	public static int passiveHashCode(String[] token) {
		String[] portCommand = token[token.length - 1].replaceAll("[()]", "").split(",");
		int portNum = (Integer.parseInt(portCommand[4]) * 256) + Integer.parseInt(portCommand[5].replaceAll("\r\n", ""));
		return Integer.parseInt(portCommand[0]) ^ Integer.parseInt(portCommand[1]) ^ Integer.parseInt(portCommand[2]) ^ Integer.parseInt(portCommand[3]) ^ portNum;
	}
	
	public static int calcHashcode(InetAddress ipAddress, int portNum) { 
		String[] token = ipAddress.toString().split("\\.");
		return Integer.parseInt(token[0].substring(1)) ^ Integer.parseInt(token[1]) ^ Integer.parseInt(token[2]) ^ Integer.parseInt(token[3]) ^ portNum;
	}
}

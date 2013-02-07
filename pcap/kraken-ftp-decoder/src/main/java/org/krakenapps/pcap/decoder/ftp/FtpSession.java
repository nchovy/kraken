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
package org.krakenapps.pcap.decoder.ftp;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

/**
 * @author mindori
 */
public class FtpSession {
	private Buffer txBuffer;
	private Buffer rxBuffer;
	private Properties props;

	private Map<Integer, FtpDataSession> dataSessions;

	public FtpSession() {
		txBuffer = new ChainBuffer();
		rxBuffer = new ChainBuffer();
		props = new Properties();
		
		dataSessions = new HashMap<Integer, FtpDataSession>();
	}

	public Buffer getTxBuffer() {
		return txBuffer;
	}

	public Buffer getRxBuffer() {
		return rxBuffer;
	}

	public Properties getProps() {
		return props;
	}

	public void createDataSessions() { 
		dataSessions = new HashMap<Integer, FtpDataSession>();
	}
	
	public FtpDataSession getDataSession(int port) {
		return dataSessions.get(port);
	}

	public void putDataSession(int serverPort, FtpDataSession dataSession) {
		dataSessions.put(serverPort, dataSession);
	}
	
	public void removeDataSession(int serverPort) { 
		dataSessions.remove(serverPort);
	}
	
	public boolean containsPort(int serverPort) { 
		return dataSessions.containsKey(serverPort);
	}
	
	public void clear() { 
		txBuffer = null;
		rxBuffer = null;	
	}
	
	public void reset() {
		txBuffer = null;
		rxBuffer = null;
		
		txBuffer = new ChainBuffer();
		rxBuffer = new ChainBuffer();
	}
}

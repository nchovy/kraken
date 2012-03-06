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
package org.krakenapps.pcap.decoder.smtp.impl;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

/**
 * @author mindori
 */
public class SmtpSession {
	private Buffer txBuffer;
	private Buffer rxBuffer;

	private boolean isDataMode;

	public SmtpSession() {
		txBuffer = new ChainBuffer();
		rxBuffer = new ChainBuffer();
	}

	public boolean isDataMode() {
		return isDataMode;
	}

	public void setDataMode(boolean isDataMode) {
		this.isDataMode = isDataMode;
	}

	public Buffer getTxBuffer() {
		return txBuffer;
	}

	public Buffer getRxBuffer() {
		return rxBuffer;
	}

	public void reset() { 
		txBuffer = null;
		rxBuffer = null;
		
		txBuffer = new ChainBuffer();
		rxBuffer = new ChainBuffer();
	}
	
	public void resetTx() { 
		txBuffer = null;
		txBuffer = new ChainBuffer();
	}
	
	public void resetRx() { 
		rxBuffer = null;
		rxBuffer = new ChainBuffer();
	}
	
	public void clear() {
		txBuffer = null;
		rxBuffer = null;
	}
}
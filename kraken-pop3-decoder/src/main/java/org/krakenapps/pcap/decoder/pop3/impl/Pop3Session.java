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
package org.krakenapps.pcap.decoder.pop3.impl;

import org.krakenapps.pcap.util.ChainBuffer;

/**
 * @author mindori
 */
public class Pop3Session {
	private ChainBuffer txBuffer;
	private ChainBuffer rxBuffer;
	
	private Pop3State state;

	private boolean isSkipRETRMessage = false;
	/* remark start point of e-mail */
	private boolean remarkStart = false;
	
	public Pop3Session() {
		txBuffer = new ChainBuffer();
		rxBuffer = new ChainBuffer();
			
		state = Pop3State.NONE;
	}

	public ChainBuffer getTxBuffer() {
		return txBuffer;
	}

	public ChainBuffer getRxBuffer() {
		return rxBuffer;
	}

	public Pop3State getState() {
		return state;
	}

	public void setState(Pop3State state) {
		this.state = state;
	}
	
	public boolean isSkipRETRMessage() {
		return isSkipRETRMessage;
	}

	public void setSkipRETRMessage(boolean isSkipRETRMessage) {
		this.isSkipRETRMessage = isSkipRETRMessage;
	}

	public boolean isRemarkStart() {
		return remarkStart;
	}

	public void setRemarkStart(boolean remarkStart) {
		this.remarkStart = remarkStart;
	}
	
	public void initEmailVars() {
		remarkStart = false;
	}
	
	public void clear() { 
		txBuffer = null;
		rxBuffer = null;
		
		txBuffer = new ChainBuffer();
		rxBuffer = new ChainBuffer();
	}
}

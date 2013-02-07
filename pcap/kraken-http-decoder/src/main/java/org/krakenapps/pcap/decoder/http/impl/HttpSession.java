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
package org.krakenapps.pcap.decoder.http.impl;

import java.net.InetSocketAddress;
import java.util.Properties;

import org.krakenapps.pcap.util.ChainBuffer;

/**
 * @author mindori
 */
public class HttpSession {
	private InetSocketAddress clientAddr;
	private InetSocketAddress serverAddr;
	private HttpRequestImpl request;
	private HttpResponseImpl response;

	private Properties props;
	private ChainBuffer txBuffer;
	private ChainBuffer rxBuffer;

	private HttpRequestState requestState;
	private HttpResponseState responseState;

	public HttpSession(InetSocketAddress clientAddr, InetSocketAddress serverAddr) {
		this.clientAddr = clientAddr;
		this.serverAddr = serverAddr;
		props = new Properties();

		txBuffer = new ChainBuffer();
		rxBuffer = new ChainBuffer();

		requestState = HttpRequestState.READY;
		responseState = HttpResponseState.READY;
	}

	public HttpRequestImpl getRequest() {
		return request;
	}

	public void createRequest() {
		request = new HttpRequestImpl(clientAddr, serverAddr);
	}

	public HttpResponseImpl getResponse() {
		return response;
	}

	public void createResponse() {
		response = new HttpResponseImpl();
	}

	public void removeHttpMessages() {
		request = null;
		response = null;
	}

	public Properties getProps() {
		return props;
	}

	public ChainBuffer getTxBuffer() {
		return txBuffer;
	}

	public void setTxBuffer(ChainBuffer txBuffer) {
		this.txBuffer = txBuffer;
	}

	public ChainBuffer getRxBuffer() {
		return rxBuffer;
	}

	public void setRxBuffer(ChainBuffer rxBuffer) {
		this.rxBuffer = rxBuffer;
	}

	public HttpRequestState getRequestState() {
		return requestState;
	}

	public void setRequestState(HttpRequestState requestState) {
		this.requestState = requestState;
	}

	public HttpResponseState getResponseState() {
		return responseState;
	}

	public void setResponseState(HttpResponseState responseState) {
		this.responseState = responseState;
	}

	public void deallocate() {
		txBuffer = null;
		rxBuffer = null;
	}
}

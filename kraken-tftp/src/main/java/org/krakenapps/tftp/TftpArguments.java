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
package org.krakenapps.tftp;

public class TftpArguments {
	private TftpMode mode;
	private String host;
	private String source;
	private String destination;

	public TftpArguments(String host, String source) { 
		mode = TftpMode.NETASCII;
		this.host = host;
		this.source = source;
	}
	
	public TftpArguments(String host, String source, String destination) { 
		this(host, source);
		this.destination = destination;
	}
	
	public TftpArguments(TftpMode mode, String host, String source) { 
		this(host, source);
		this.mode = mode;
	}
	
	public TftpArguments(TftpMode mode, String host, String source, String destination) {
		this(host, source, destination);
		this.mode = mode;
	}
	
	public TftpMode getMode() {
		return mode;
	}
	
	public String getHost() {
		return host;
	}
	
	public String getSource() {
		return source;
	}
	
	public String getDestination() {
		return destination;
	}
}
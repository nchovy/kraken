/*
 * Copyright 2008 NCHOVY
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
package org.krakenapps.xmlrpc;

import java.util.List;

enum MessageType {
	MethodCall,
	MethodResponse
}

public class XmlRpcMessage {
	private MessageType type;
	private String methodName;
	private List<Object> parameters;
	
	public XmlRpcMessage(MessageType type) {
		this.type = type;
	}
	
	public MessageType getType() {
		return type;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public Object[] getParameters() {
		return parameters.toArray(); 
	}
	
	public void setParameters(List<Object> parameters) {
		this.parameters = parameters;
	}
}

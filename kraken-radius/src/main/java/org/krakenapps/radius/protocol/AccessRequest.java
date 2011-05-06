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
package org.krakenapps.radius.protocol;

public class AccessRequest extends RadiusPacket {
	private UserNameAttribute userName;
	private UserPasswordAttribute userPassword;
	private NasPortAttribute nasPort;
	private NasPortTypeAttribute nasPortType;

	public AccessRequest() {
		setCode(1);
	}

	public UserNameAttribute getUserName() {
		return userName;
	}

	public void setUserName(UserNameAttribute userName) {
		this.userName = userName;
	}

	public UserPasswordAttribute getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(UserPasswordAttribute userPassword) {
		this.userPassword = userPassword;
	}

	public NasPortAttribute getNasPort() {
		return nasPort;
	}

	public void setNasPort(NasPortAttribute nasPort) {
		this.nasPort = nasPort;
	}

	public NasPortTypeAttribute getNasPortType() {
		return nasPortType;
	}

	public void setNasPortType(NasPortTypeAttribute nasPortType) {
		this.nasPortType = nasPortType;
	}
}

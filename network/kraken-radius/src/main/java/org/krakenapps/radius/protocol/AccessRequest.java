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
	public AccessRequest() {
		setCode(1);
		setAuthenticator(newRequestAuthenticator());
	}

	public byte[] getRequestAuthenticator() {
		return getAuthenticator();
	}

	public UserNameAttribute getUserName() {
		return (UserNameAttribute) findAttribute(1);
	}

	public void setUserName(UserNameAttribute userName) {
		getAttributes().add(userName);
	}

	public UserPasswordAttribute getUserPassword() {
		return (UserPasswordAttribute) findAttribute(2);
	}

	public void setUserPassword(UserPasswordAttribute userPassword) {
		getAttributes().add(userPassword);
	}

	public ChapPasswordAttribute getChapPassword() {
		return (ChapPasswordAttribute) findAttribute(3);
	}

	public void setChapPassword(ChapPasswordAttribute chapPassword) {
		getAttributes().add(chapPassword);
	}

	public NasIpAddressAttribute getNasIpAddress() {
		return (NasIpAddressAttribute) findAttribute(4);
	}

	public void setNasIpAddress(NasIpAddressAttribute nasIpAddress) {
		getAttributes().add(nasIpAddress);
	}

	public NasIdentifierAttribute getNasIdentifier() {
		return (NasIdentifierAttribute) findAttribute(32);
	}

	public void setNasIdentifier(NasIdentifierAttribute nasIdentifier) {
		getAttributes().add(nasIdentifier);
	}

	public NasPortAttribute getNasPort() {
		return (NasPortAttribute) findAttribute(5);
	}

	public void setNasPort(NasPortAttribute nasPort) {
		getAttributes().add(nasPort);
	}

	public NasPortTypeAttribute getNasPortType() {
		return (NasPortTypeAttribute) findAttribute(61);
	}

	public void setNasPortType(NasPortTypeAttribute nasPortType) {
		getAttributes().add(nasPortType);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("AccessRequest: ");

		int i = 0;
		for (RadiusAttribute attr : getAttributes()) {
			if (i++ != 0)
				sb.append(", ");
			sb.append("(");
			sb.append(attr);
			sb.append(")");
		}

		return sb.toString();
	}
}

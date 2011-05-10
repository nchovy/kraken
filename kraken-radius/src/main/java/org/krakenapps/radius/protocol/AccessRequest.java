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

import java.util.ArrayList;
import java.util.List;

public class AccessRequest extends RadiusPacket {
	private byte[] authenticator;
	private UserNameAttribute userName;
	private UserPasswordAttribute userPassword;
	private NasIpAddressAttribute nasIpAddress;
	private NasIdentifierAttribute nasIdentifier;
	private NasPortAttribute nasPort;
	private NasPortTypeAttribute nasPortType;

	public AccessRequest() {
		setCode(1);
		authenticator = newRequestAuthenticator();
	}

	public byte[] getRequestAuthenticator() {
		return authenticator;
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

	public NasIpAddressAttribute getNasIpAddress() {
		return nasIpAddress;
	}

	public void setNasIpAddress(NasIpAddressAttribute nasIpAddress) {
		this.nasIpAddress = nasIpAddress;
	}

	public NasIdentifierAttribute getNasIdentifier() {
		return nasIdentifier;
	}

	public void setNasIdentifier(NasIdentifierAttribute nasIdentifier) {
		this.nasIdentifier = nasIdentifier;
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

	@Override
	public List<RadiusAttribute> getAttributes() {
		List<RadiusAttribute> attrs = new ArrayList<RadiusAttribute>(super.getAttributes());
		attrs.add(userName);

		// User-Password or CHAP
		if (userPassword != null)
			attrs.add(userPassword);
		
		// NAS-IP-Address or NAS-Identifier
		if (nasIpAddress != null)
			attrs.add(nasIpAddress);
		
		if (nasIdentifier != null)
			attrs.add(nasIdentifier);
		
		// NAS-Port or NAS-Port-Type
		if (nasPort != null)
			attrs.add(nasPort);
		
		if (nasPortType != null)
			attrs.add(nasPortType);
		
		return attrs;
	}

}

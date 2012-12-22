/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.dns;

public enum DnsResponseCode {
	NO_ERROR(0, "No Error"), FORMAT_ERROR(1, "Format Error"), SERVER_FAILURE(2, "Server Failure"), NAME_ERROR(3, "Name Error"), NOT_IMPLEMENTED(
			4, "Not Implemented"), REFUSED(5, "Refused");

	DnsResponseCode(int code, String description) {
		this.code = code;
		this.description = description;
	}

	public static DnsResponseCode parse(int code) {
		for (DnsResponseCode c : values())
			if (c.getCode() == code)
				return c;

		return null;
	}

	public int getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	private int code;
	private String description;
}

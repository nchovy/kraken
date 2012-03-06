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

public class AccessReject extends RadiusResponse {
	public AccessReject() {
		setCode(3);
	}

	public AccessReject(RadiusPacket req, String sharedSecret) {
		super(req, sharedSecret);
		setCode(3);
	}

	@Override
	public String toString() {
		RadiusAttribute replyMessage = findAttribute(18);
		if (replyMessage != null)
			return "Access-Reject: " + replyMessage;

		return "Access-Reject";
	}
}

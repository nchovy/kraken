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
package org.krakenapps.radius.server.auth;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.radius.protocol.AccessAccept;
import org.krakenapps.radius.protocol.AccessReject;
import org.krakenapps.radius.protocol.AccessRequest;
import org.krakenapps.radius.protocol.RadiusResponse;
import org.krakenapps.radius.protocol.ReplyMessageAttribute;
import org.krakenapps.radius.server.RadiusAuthenticator;
import org.krakenapps.radius.server.RadiusAuthenticatorFactory;
import org.krakenapps.radius.server.RadiusProfile;
import org.krakenapps.radius.server.RadiusUserDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PapAuthenticator extends RadiusAuthenticator {
	private final Logger logger = LoggerFactory.getLogger(PapAuthenticator.class.getName());

	private Date lastAuth;
	private AtomicInteger counter;

	public PapAuthenticator(String name, RadiusAuthenticatorFactory factory) {
		super(name, factory);

		this.lastAuth = null;
		this.counter = new AtomicInteger();
	}

	@Override
	public RadiusResponse authenticate(RadiusProfile profile, AccessRequest req, List<RadiusUserDatabase> userDatabases) {
		// for statistics
		lastAuth = new Date();
		counter.incrementAndGet();

		// authentication
		String name = req.getUserName().getName();
		String password = req.getUserPassword().getPassword();

		// TODO: password will be moved to debug logging
		logger.info("kraken radius: pap auth for user [{}], password [{}]", name, password);

		boolean passed = false;
		for (RadiusUserDatabase userdb : userDatabases) {
			if (userdb.verifyPassword(name, password))
				passed = true;
		}

		if (passed) {
			AccessAccept response = new AccessAccept(req, profile.getSharedSecret());
			response.getAttributes().add(new ReplyMessageAttribute("Welcome to kraken-radius"));
			return response;
		} else {
			AccessReject response = new AccessReject(req, profile.getSharedSecret());
			response.getAttributes().add(new ReplyMessageAttribute("Go away!"));
			return response;
		}
	}

	@Override
	public String toString() {
		return "PAP: last auth [" + lastAuth + "], auth count [" + counter.get() + "]";
	}

}

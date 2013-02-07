/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.msgbus.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.cron.PeriodicJob;
import org.krakenapps.msgbus.Token;
import org.krakenapps.msgbus.TokenApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PeriodicJob("* * * * *")
@Component(name = "msgbus-token-api")
@Provides
public class TokenApiImpl implements TokenApi, Runnable {
	private Map<String, Token> tokens = new ConcurrentHashMap<String, Token>();
	private final Logger logger = LoggerFactory.getLogger(TokenApiImpl.class);

	@Override
	public void addToken(Token token) {
		tokens.put(token.getTokenId(), token);
	}

	@Override
	public Collection<Token> getTokens() {
		return Collections.unmodifiableCollection(tokens.values());
	}

	@Override
	public Token getToken(String tokenId) {
		return tokens.get(tokenId);
	}

	@Override
	public void removeToken(String tokenId) {
		tokens.remove(tokenId);

	}

	@Override
	public void setToken(Token token) {
		String tokenId = token.getTokenId();
		tokens.put(tokenId, token);		
	}
	
	@Override
	public void run() {
		long now = new Date().getTime();
		for (Token token : getTokens()) {
			long issuedTime = token.getIssuedDate().getTime();

			if (now - issuedTime > 30 * 60 * 1000) {
				removeToken(token.getTokenId());
				logger.trace("kraken msgbus: remove token [{}]", token);
			}
		}

	}

}

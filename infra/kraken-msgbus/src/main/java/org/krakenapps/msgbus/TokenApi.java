package org.krakenapps.msgbus;

import java.util.Collection;

public interface TokenApi {
	void addToken(Token token);

	Collection<Token> getTokens();

	Token getToken(String tokenId);

	void removeToken(String tokenId);
}

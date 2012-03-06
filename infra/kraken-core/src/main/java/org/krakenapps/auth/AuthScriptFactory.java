package org.krakenapps.auth;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.auth.api.AuthService;

public class AuthScriptFactory implements ScriptFactory {

	private AuthService auth;

	public AuthScriptFactory(AuthService auth) {
		this.auth = auth;
	}

	@Override
	public Script createScript() {
		return new AuthScript(auth);
	}
}

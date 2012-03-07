package org.krakenapps.auth;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.auth.api.AuthProfile;
import org.krakenapps.auth.api.AuthProvider;
import org.krakenapps.auth.api.AuthService;
import org.krakenapps.auth.api.AuthStrategy;

public class AuthScript implements Script {
	private AuthService auth;
	private ScriptContext context;

	public AuthScript(AuthService auth) {
		this.auth = auth;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void profiles(String[] args) {
		for (AuthProfile p : auth.getProfiles())
			context.println(p);
	}

	public void providers(String[] args) {
		for (AuthProvider p : auth.getProviders())
			context.println(p);
	}

	@ScriptUsage(description = "create auth profile", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "profile name"),
			@ScriptArgument(name = "strategy", type = "string", description = "all or any"),
			@ScriptArgument(name = "providers", type = "string", description = "provider name") })
	public void createProfile(String[] args) {
		AuthStrategy strategy = null;
		if (args[1].equalsIgnoreCase("all"))
			strategy = AuthStrategy.MatchAll;
		else
			strategy = AuthStrategy.MatchAny;

		List<String> providers = new ArrayList<String>();
		for (int i = 2; i < args.length; i++)
			if (auth.getProvider(args[i]) != null)
				providers.add(args[i]);

		AuthProfile p = new AuthProfile();
		p.setName(args[0]);
		p.setStrategy(strategy);
		p.setProviders(providers);
		auth.createProfile(p);
		context.println("created");
	}

	public void removeProfile(String[] args) {

	}
}

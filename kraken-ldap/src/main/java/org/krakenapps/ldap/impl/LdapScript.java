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
package org.krakenapps.ldap.impl;

import java.util.Collection;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.ldap.DomainOrganizationalUnit;
import org.krakenapps.ldap.DomainUserAccount;
import org.krakenapps.ldap.LdapProfile;
import org.krakenapps.ldap.LdapService;
import org.osgi.framework.BundleContext;

public class LdapScript implements Script {
	private BundleContext bc;
	private LdapService ldap;
	private ScriptContext context;

	public LdapScript(BundleContext bc, LdapService ldap) {
		this.bc = bc;
		this.ldap = ldap;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void profiles(String[] args) {
		context.println("LDAP Profiles");
		context.println("-----------------");
		for (LdapProfile profile : ldap.getProfiles()) {
			context.println(profile.toString());
		}
	}

	@ScriptUsage(description = "create ldap profile", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "ldap profile name"),
			@ScriptArgument(name = "dc", type = "string", description = "domain name of domain controller "),
			@ScriptArgument(name = "account", type = "string", description = "admin account name for simple bind (e.g. OFFICE\\xeraph"),
			@ScriptArgument(name = "password", type = "string", description = "admin password") })
	public void createProfile(String[] args) {
		String name = args[0];
		String dc = args[1];
		String account = args[2];
		String password = args[3];

		LdapProfile profile = new LdapProfile(name, dc, account, password);
		ldap.createProfile(profile);
		context.println("created");
	}

	@ScriptUsage(description = "remove ldap profile", arguments = { @ScriptArgument(name = "name", type = "string", description = "ldap profile name") })
	public void removeProfile(String[] args) {
		String name = args[0];
		ldap.removeProfile(name);
		context.println("removed");
	}

	@ScriptUsage(description = "print all domain users", arguments = { @ScriptArgument(name = "profile name", type = "string", description = "profile name") })
	public void domainUsers(String[] args) {
		String profileName = args[0];
		LdapProfile profile = ldap.getProfile(profileName);
		if (profile == null) {
			context.println("profile not found.");
			return;
		}

		Collection<DomainUserAccount> accounts = ldap.getDomainUserAccounts(profile);
		if (accounts == null) {
			context.println("domain users not found");
			return;
		}

		context.println("Domain Users");
		context.println("--------------------");
		for (DomainUserAccount account : accounts)
			context.println(account.toString());
	}

	@ScriptUsage(description = "print all organization units", arguments = { @ScriptArgument(name = "profile name", type = "string", description = "profile name") })
	public void organizationUnits(String[] args) {
		String profileName = args[0];
		LdapProfile profile = ldap.getProfile(profileName);
		if (profile == null) {
			context.println("profile not found.");
			return;
		}

		Collection<DomainOrganizationalUnit> ous = ldap.getOrganizationUnits(profile);
		if (ous == null) {
			context.println("organization units not found");
			return;
		}

		context.println("Organization Units");
		context.println("--------------------");
		for (DomainOrganizationalUnit ou : ous)
			context.println(ou);
	}

	@ScriptUsage(description = "print all domain users", arguments = {
			@ScriptArgument(name = "profile name", type = "string", description = "profile name"),
			@ScriptArgument(name = "account", type = "string", description = "account name without domain prefix (e.g. xeraph)"),
			@ScriptArgument(name = "password", type = "string", description = "test password") })
	public void verifyPassword(String[] args) {
		String profileName = args[0];
		String account = args[1];
		String password = args[2];

		LdapProfile profile = ldap.getProfile(profileName);
		if (profile == null) {
			context.println("profile not found.");
			return;
		}

		boolean success = ldap.verifyPassword(profile, account, password);
		if (success)
			context.println("valid password");
		else
			context.println("invalid password");
	}

	@ScriptUsage(description = "sync all organization units with kraken-dom", arguments = { @ScriptArgument(name = "profile name", type = "string", description = "profile name") })
	public void syncDom(String args[]) {
		String profileName = args[0];
		LdapProfile profile = ldap.getProfile(profileName);
		if (profile == null) {
			context.println("profile not found.");
			return;
		}

		ldap.sync(bc, profile);
	}
}

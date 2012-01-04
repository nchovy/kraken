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

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.ldap.DomainOrganizationalUnit;
import org.krakenapps.ldap.DomainUserAccount;
import org.krakenapps.ldap.LdapProfile;
import org.krakenapps.ldap.LdapService;
import org.krakenapps.ldap.LdapSyncService;
import org.krakenapps.ldap.LdapProfile.CertificateType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

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
			@ScriptArgument(name = "password", type = "string", description = "admin password"),
			@ScriptArgument(name = "truststore path", type = "string", description = "truststore file path", optional = true) })
	public void createProfile(String[] args) {
		try {
			LdapProfile profile = new LdapProfile();
			profile.setName(args[0]);
			profile.setDc(args[1]);
			profile.setAccount(args[2]);
			profile.setPassword(args[3]);
			if (args.length > 4) {
				File file = new File(args[4]);
				if (!file.exists())
					throw new IllegalArgumentException("file not found");
				profile.setTrustStore(CertificateType.X509, new FileInputStream(file));
			}

			ldap.createProfile(profile);
			context.println("created");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
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
	public void sync(String args[]) {
		LdapSyncService ldapSync = getSyncService();
		if (ldapSync == null) {
			context.println("kraken-dom not found");
			return;
		}

		String profileName = args[0];
		LdapProfile profile = ldap.getProfile(profileName);
		ldapSync.sync(profile);
	}

	public void periodicSync(String[] args) {
		LdapSyncService ldapSync = getSyncService();
		context.println(ldapSync.getPeriodicSync() ? "enabled" : "disabled");
	}

	@ScriptUsage(description = "activate or deactivate periodic sync", arguments = { @ScriptArgument(name = "activate flag", type = "string", description = "true or false") })
	public void setPeriodicSync(String[] args) {
		LdapSyncService ldapSync = getSyncService();
		ldapSync.setPeriodicSync(Boolean.parseBoolean(args[0]));
		context.println("set");
	}

	@ScriptUsage(description = "set sync interval", arguments = {
			@ScriptArgument(name = "profile name", type = "string", description = "profile name"),
			@ScriptArgument(name = "sync interval", type = "int", description = "sync interval in milliseconds") })
	public void setSyncInterval(String[] args) {
		LdapProfile p = ldap.getProfile(args[0]);
		if (p == null) {
			context.println("profile not found");
			return;
		}

		try {
			p.setSyncInterval(Integer.valueOf(args[1]));
			ldap.updateProfile(p);
			context.println("set");
		} catch (NumberFormatException e) {
			context.println("invalid number format");
		}
	}

	private LdapSyncService getSyncService() {
		ServiceReference ref = bc.getServiceReference(LdapSyncService.class.getName());
		if (ref == null)
			throw new IllegalStateException("kraken-dom not found");

		return (LdapSyncService) bc.getService(ref);
	}
}

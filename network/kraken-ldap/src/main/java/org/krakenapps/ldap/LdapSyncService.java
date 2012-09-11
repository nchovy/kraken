package org.krakenapps.ldap;

public interface LdapSyncService {
	void sync(LdapProfile profile);
	
	void unsync(LdapProfile profile);

	void unsyncAll();
}

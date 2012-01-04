package org.krakenapps.ldap;

public interface LdapSyncService {
	boolean getPeriodicSync();

	void setPeriodicSync(boolean activate);

	void sync(LdapProfile profile);
}

package org.krakenapps.ldap;

import org.krakenapps.dom.api.UserExtensionProvider;

public interface LdapSyncService extends UserExtensionProvider {
	boolean getPeriodicSync();

	void setPeriodicSync(boolean activate);

	void sync(LdapProfile profile);

	void importLdap(LdapProfile profile);

	void exportDom(LdapProfile profile);
}

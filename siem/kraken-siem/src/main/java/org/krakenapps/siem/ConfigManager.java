package org.krakenapps.siem;

import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.msgbus.Session;

public interface ConfigManager {
	/**
	 * @param session
	 *            session for multi-tenancy support
	 * @return the config database instance
	 */
	ConfigDatabase getDatabase(Session session);

	/**
	 * will be removed after multi-tenancy support
	 */
	@Deprecated
	ConfigDatabase getDatabase();
}

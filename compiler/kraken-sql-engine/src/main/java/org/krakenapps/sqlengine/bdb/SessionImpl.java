package org.krakenapps.sqlengine.bdb;

import java.sql.SQLException;

import org.krakenapps.sqlengine.CursorHandle;
import org.krakenapps.sqlengine.DatabaseHandle;
import org.krakenapps.sqlengine.Session;

public class SessionImpl implements Session {
	private DatabaseHandle databaseHandle;

	public SessionImpl(DatabaseHandle databaseHandle) {
		this.databaseHandle = databaseHandle;
	}

	/* (non-Javadoc)
	 * @see org.krakenapps.sqlengine.impl.Session#getDatabaseName()
	 */
	public String getDatabaseName() {
		return databaseHandle.getName();
	}

	/* (non-Javadoc)
	 * @see org.krakenapps.sqlengine.impl.Session#openFor(java.lang.String)
	 */
	public CursorHandle openFor(String sql) throws SQLException {
		return databaseHandle.openFor(this, sql);
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		return databaseHandle.execute(this, sql);
	}
	
	
}

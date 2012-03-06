package org.krakenapps.sqlengine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import org.krakenapps.sqlengine.bdb.ConnectionImpl;
import org.krakenapps.sqlengine.bdb.SqlEngine;

public class Driver implements java.sql.Driver {
	private SqlEngine engine = new SqlEngine();
	
	static {
		try {
			DriverManager.registerDriver(new Driver());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		int p = url.indexOf("://");
		if (p < 0)
			throw new SQLException("invalid jdbc connection string: " + url);

		String dbName = url.substring(p + 3).split("[/;]")[1];
		return new ConnectionImpl(engine.createSession(dbName));
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		return url.startsWith("jdbc:kraken-sql://");
	}

	@Override
	public int getMajorVersion() {
		return 1;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		return null;
	}

	@Override
	public boolean jdbcCompliant() {
		return false;
	}

}

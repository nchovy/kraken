package org.krakenapps.sqlengine.bdb;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.sqlengine.DatabaseHandle;
import org.krakenapps.sqlengine.Session;

public class SqlEngine {
	private ConcurrentMap<String, DatabaseHandle> databaseMap;

	public SqlEngine() {
		databaseMap = new ConcurrentHashMap<String, DatabaseHandle>();
	}

	public DatabaseHandle getDatabase(String name) {
		databaseMap.putIfAbsent(name, new DatabaseHandleImpl(name));
		return databaseMap.get(name);
	}

	public Session createSession(String databaseName) {
		return new SessionImpl(getDatabase(databaseName));
	}
	
	public void close() {
		
	}
}

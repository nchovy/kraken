package org.krakenapps.sqlengine.bdb;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.sqlengine.CursorHandle;
import org.krakenapps.sqlengine.DatabaseHandle;
import org.krakenapps.sqlengine.Session;
import org.krakenapps.sqlengine.TableHandle;
import org.krakenapps.sqlengine.TableHandleEventListener;
import org.krakenapps.sqlengine.TableSchemaManager;
import org.krakenapps.sqlparser.SqlParser;
import org.krakenapps.sqlparser.ast.AlterTableStatement;
import org.krakenapps.sqlparser.ast.DescTableStatement;
import org.krakenapps.sqlparser.ast.DropTableStatement;
import org.krakenapps.sqlparser.ast.ShowTablesStatement;
import org.krakenapps.sqlparser.ast.TableDefinition;

import com.sleepycat.je.CheckpointConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class DatabaseHandleImpl implements DatabaseHandle, TableHandleEventListener {
	private String databaseName;
	private Environment env;
	private SqlParser parser;
	private TableSchemaManager tableSchemaManager;

	private Set<TableHandle> openedTables;

	public DatabaseHandleImpl(String databaseName) {
		this.parser = new SqlParser();
		this.databaseName = databaseName;
		this.openedTables = Collections.newSetFromMap(new ConcurrentHashMap<TableHandle, Boolean>());

		File home = new File("data/kraken-sqlengine/" + databaseName);
		home.mkdirs();

		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		envConfig.setTransactional(true);
		this.env = new Environment(home, envConfig);
		this.tableSchemaManager = new TableSchemaManagerImpl(this);
	}

	@Override
	public TableSchemaManager getTableSchemaManager() {
		return tableSchemaManager;
	}

	@Override
	public int execute(Session session, String sql) throws SQLException {
		Object stmt = null;
		try {
			stmt = parser.eval(sql);
		} catch (ParseException e) {
			throw new SQLException("invalid sql syntax: " + sql);
		}

		if (stmt instanceof TableDefinition) {
			DdlHandler.handleCreateTable(this, (TableDefinition) stmt);
			return 0;
		} else if (stmt instanceof AlterTableStatement) {
			DdlHandler.handleAlterTable(this, (AlterTableStatement) stmt);
			return 0;
		} else if (stmt instanceof DropTableStatement) {
			DdlHandler.handleDropTable(this, (DropTableStatement) stmt);
			return 0;
		}

		return -1;
	}

	@Override
	public CursorHandle openFor(Session session, String sql) throws SQLException {
		try {
			ResultMetadata metadata = new ResultMetadata();
			Iterator<Row> it = handle(sql, metadata);

			return new CursorImpl(metadata, it);
		} catch (Exception e) {
			throw new SQLException(e.getMessage(), e);
		}
	}

	private Iterator<Row> handle(String sql, ResultMetadata metadata) throws SQLException {
		Object stmt = null;
		try {
			stmt = parser.eval(sql);
		} catch (ParseException e) {
			throw new SQLException("invalid sql syntax [" + sql + "]");
		}

		if (stmt instanceof ShowTablesStatement) {
			return DdlHandler.handleShowTable(this, (ShowTablesStatement) stmt, metadata);
		} else if (stmt instanceof DescTableStatement) {
			return DdlHandler.handleDescTable(this, (DescTableStatement) stmt, metadata);
		}

		throw new SQLException("not implemented yet");
	}

	@Override
	public String getName() {
		return databaseName;
	}

	@Override
	public TableHandle openTable(String tableName) {
		return openTable(tableName, false);
	}

	@Override
	public TableHandle openTable(String tableName, boolean allowCreate) {
		return open(tableName, allowCreate);
	}

	@Override
	public TableHandle createTable(String tableName) {
		return open(tableName, true);
	}

	private TableHandle open(String tableName, boolean allowCreate) {
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(allowCreate);
		dbConfig.setTransactional(true);
		Database db = env.openDatabase(null, tableName, dbConfig);
		env.checkpoint(new CheckpointConfig().setForce(true));
		TableHandle handle = new TableHandleImpl(db);

		// add to handle map
		openedTables.add(handle);
		handle.addListener(this);
		return handle;
	}

	@Override
	public void close() {
		env.close();
	}

	//
	// receives table handle closed event
	//

	@Override
	public void onClose(TableHandle handle) {
		openedTables.remove(handle);
	}

}

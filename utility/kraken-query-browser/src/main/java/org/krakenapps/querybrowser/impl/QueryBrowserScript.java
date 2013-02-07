/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.querybrowser.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.querybrowser.ConnectionStringRegistry;

public class QueryBrowserScript implements Script {
	private ConnectionStringRegistry registry;
	private ScriptContext context;

	public QueryBrowserScript(ConnectionStringRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void setScriptContext(ScriptContext sc) {
		this.context = sc;
	}

	@ScriptUsage(description = "list all connection strings")
	public void list(String[] args) {
		context.println("Connection Strings");
		context.println("--------------------");
		for (String name : registry.getNames()) {
			context.printf("%s: %s\n", name, registry.getConnectionString(name));
		}
	}

	@ScriptUsage(description = "add new connection string", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "the name of new connection string"),
			@ScriptArgument(name = "driver", type = "string", description = "the class name of jdbc driver"),
			@ScriptArgument(name = "connection string", type = "string", description = "jdbc connection string") })
	public void add(String[] args) {
		String name = args[0];
		String driver = args[1];
		String connectionString = args[2];

		try {
			Properties props = new Properties();
			props.put("driver", driver);
			props.put("connection_string", connectionString);
			registry.setConnectionString(name, props);
			context.println("new connection string added: " + name);
		} catch (IllegalStateException e) {
			context.println("name already exists: " + name);
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "remove connection string", arguments = { @ScriptArgument(name = "name", type = "string", description = "the name of connection string") })
	public void remove(String[] args) {
		String name = args[0];
		try {
			registry.removeConnectionString(name);
		} catch (IllegalStateException e) {
			context.println("connection string not found");
		} catch (Exception e) {
			context.println(e.toString());
		}
	}

	@ScriptUsage(description = "connect to database", arguments = { @ScriptArgument(name = "name", description = "the name of the connection string") })
	public void connect(String[] args) {
		Connection conn = null;
		Properties props = registry.getConnectionString(args[0]);
		if (props == null) {
			context.println("connection string not found. please check 'qb.list'");
			return;
		}

		String driver = props.getProperty("driver");
		String connectionString = props.getProperty("connection_string");
		String user = null;
		String password = null;

		try {
			context.print("user: ");
			user = context.readLine();

			context.print("password: ");
			context.turnEchoOff();
			password = context.readLine();
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} finally {
			context.turnEchoOn();
		}

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(connectionString, user, password);
		} catch (ClassNotFoundException e1) {
			context.println("jdbc driver not found: " + driver);
			return;
		} catch (SQLException e) {
			context.println(e.getMessage());
			return;
		}

		try {
			while (true) {
				runConsole(conn);
			}
		} catch (InterruptedException e) {
			context.println("interrupted");
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
	}

	private void runConsole(Connection conn) throws InterruptedException {
		context.print("query> ");

		String sql = "";
		while (true) {
			String line = context.readLine().trim();
			sql += line + " ";

			if (line.endsWith(";"))
				break;
			
			context.print("query* ");
		}

		if (sql.substring(0, 6).equalsIgnoreCase("select")) {
			executeQuery(conn, sql);
		} else {
			executeStmt(conn, sql);
		}
	}

	private void executeStmt(Connection conn, String sql) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			context.println(e.getMessage());
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void executeQuery(Connection conn, String sql) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData m = rs.getMetaData();
			int cols = m.getColumnCount();

			for (int i = 0; i < cols; i++) {
				if (i != 0)
					context.print(", ");

				String name = m.getColumnName(i + 1);
				context.print(name);
			}
			context.println("");
			context.println("----------------------------------");
			while (rs.next()) {
				for (int i = 0; i < cols; i++) {
					String value = rs.getString(i + 1);
					if (i != 0)
						context.print(", ");

					context.print(value != null ? value : "null");
				}
				context.println("");
			}

		} catch (SQLException e) {
			context.println(e.getMessage());
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
		}
	}

}

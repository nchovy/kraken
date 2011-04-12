package org.krakenapps.sqlengine.bdb;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class SqlEngineRunner {
	private static final String PROMPT = "SQL_2002-12770";

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		Class.forName("org.krakenapps.sqlengine.Driver");
		Connection conn = DriverManager.getConnection("jdbc:kraken-sql://localhost/test");

		System.out.println("SQL Engine");
		System.out.println("-----------------");

		try {
			while (true) {
				System.out.print(PROMPT + "> ");
				handleInput(conn);
			}
		} catch (EOFException e) {
			System.out.println("Interrupted");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void handleInput(Connection conn) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String line = br.readLine();
			if (line == null)
				throw new EOFException();

			sb.append(' ');
			sb.append(line.trim());
			if (line.endsWith(";")) {
				break;
			}

			System.out.print(PROMPT + "# ");
		}

		// remove last semicolon
		String sql = sb.toString().trim();
		sql = sql.substring(0, sql.length() - 1);

		String type = sql.split(" ")[0];

		try {
			long begin = new Date().getTime();
			if (type.equalsIgnoreCase("SELECT") || type.equalsIgnoreCase("SHOW") || type.equalsIgnoreCase("DESC")) {
				handleSelect(conn, sql);
			} else {
				handleUpdate(conn, sql);
			}
			long end = new Date().getTime();
			String msec = String.format("%.2f", (end - begin) / 1000.0);
			System.out.println("(" + msec + " sec)");
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
//			e.printStackTrace();
		}
	}

	private static void handleUpdate(Connection conn, String sql) throws SQLException {
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sql);
	}

	private static void handleSelect(Connection conn, String sql) throws SQLException {

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		
		// check
		if (!rs.next()) {
			System.out.println("empty set");
			return;
		}

		ResultSetMetaData metadata = rs.getMetaData();
		if (metadata != null && metadata.getColumnCount() == 0) {
			System.out.println("empty set");
			return;
		}

		int[] displaySizes = new int[metadata.getColumnCount()];
		int lineLength = 1;
		for (int i = 1; i <= metadata.getColumnCount(); i++) {
			displaySizes[i - 1] = metadata.getColumnDisplaySize(i);
			lineLength += displaySizes[i - 1] + 3;
		}

		drawLine(lineLength);
		for (int i = 1; i <= metadata.getColumnCount(); i++) {
			if (i == 1)
				System.out.print("| ");

			System.out.print(metadata.getColumnName(i));
			
			System.out.print(" | ");
		}
		System.out.println();

		drawLine(lineLength);

		do {
			for (int i = 1; i <= metadata.getColumnCount(); i++) {
				Object value = rs.getObject(i);

				String out = value == null ? "null" : value.toString();
				int remains = displaySizes[i - 1] - out.length();

				if (i == 1)
					System.out.print("| ");

				System.out.print(out);
				for (int j = 0; j < remains; j++)
					System.out.print(' ');

				System.out.print(" | ");
			}
			System.out.println();
		} while(rs.next());

		drawLine(lineLength);
	}

	private static void drawLine(int lineLength) {
		for (int i = 0; i < lineLength; i++)
			System.out.print("-");

		System.out.println();
	}

}

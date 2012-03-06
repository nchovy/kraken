package org.krakenapps.sqlengine.bdb;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultMetadata implements ResultSetMetaData {
	private List<String> columnNames = new ArrayList<String>();
	private Map<Integer, Integer> displaySizeMap = new HashMap<Integer, Integer>();

	@Override
	public String getCatalogName(int column) throws SQLException {
		return "default";
	}

	public void addColumn(String columnName) {
		int index = columnNames.size();
		columnNames.add(columnName);
		updateDisplaySize(index, columnName.length());
	}

	public void updateDisplaySize(int i, int size) {
		if (displaySizeMap.containsKey(i)) {
			int old = displaySizeMap.get(i);
			if (size > old)
				displaySizeMap.put(i, size);
		} else {
			displaySizeMap.put(i, size);
		}
	}

	@Override
	public String getColumnClassName(int column) throws SQLException {
		return null;
	}

	@Override
	public int getColumnCount() throws SQLException {
		return columnNames.size();
	}

	@Override
	public int getColumnDisplaySize(int column) throws SQLException {
		if (displaySizeMap.containsKey(column - 1))
			return displaySizeMap.get(column - 1);
		
		return 0;
	}

	@Override
	public String getColumnLabel(int column) throws SQLException {
		return null;
	}

	@Override
	public String getColumnName(int column) throws SQLException {
		return columnNames.get(column - 1);
	}

	@Override
	public int getColumnType(int column) throws SQLException {
		return 0;
	}

	@Override
	public String getColumnTypeName(int column) throws SQLException {
		return null;
	}

	@Override
	public int getPrecision(int column) throws SQLException {
		return 0;
	}

	@Override
	public int getScale(int column) throws SQLException {
		return 0;
	}

	@Override
	public String getSchemaName(int column) throws SQLException {
		return null;
	}

	@Override
	public String getTableName(int column) throws SQLException {
		return null;
	}

	@Override
	public boolean isAutoIncrement(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isCaseSensitive(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isCurrency(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isDefinitelyWritable(int column) throws SQLException {
		return false;
	}

	@Override
	public int isNullable(int column) throws SQLException {
		return 0;
	}

	@Override
	public boolean isReadOnly(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isSearchable(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isSigned(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isWritable(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}
}

package org.krakenapps.sqlengine.bdb;

import org.krakenapps.sqlparser.ast.DataType;
import org.krakenapps.sqlparser.ast.DateTimeDataType;
import org.krakenapps.sqlparser.ast.IntegerDataType;
import org.krakenapps.sqlparser.ast.StringDataType;

public class DataTypeSerializer {
	public static DataType deserialize(String s) {
		if (s.startsWith("CHAR")) {
			int p = s.indexOf(')');
			int len = Integer.valueOf(s.substring(5, p));
			return new StringDataType(len);
		} else if (s.equals("INT")) {
			return new IntegerDataType();
		} else if (s.equals("DATETIME")) {
			return new DateTimeDataType();
		}

		throw new UnsupportedOperationException("not supported data type: " + s);
	}

	public static String serialize(DataType type) {
		return type.toString();
	}
}

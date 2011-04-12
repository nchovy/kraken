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
package org.krakenapps.sqlparser.syntax;

import java.nio.BufferUnderflowException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.ParserContext;
import org.krakenapps.bnf.Placeholder;
import org.krakenapps.bnf.Result;
import org.krakenapps.bnf.StringUtil;

public class RegularIdentifierPlaceholder implements Placeholder {

	private static Set<String> keywords = new HashSet<String>();

	static {
		String[] words = new String[] { "ABSOLUTE", "ACTION", "ADD", "ADMIN", "AFTER", "AGGREGATE", "ALIAS", "ALL",
				"ALLOCATE", "ALTER", "AND", "ANY", "ARE", "ARRAY", "AS", "ASC", "AT", "AUTHORIZATION", "BEFORE",
				"BEGIN", "BINARY", "BIT", "BLOB", "BOOLEAN", "BOTH", "BREADTH", "BY", "CALL", "CASCADE", "CASCADED",
				"CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHECK", "CLASS", "CLOB", "CLOSE", "COLLATE",
				"COLLATION", "COLUMN", "COMMIT", "COMPLETION", "CONDITION", "CONNECT", "CONNECTION", "CONSTRAINT",
				"CONSTRAINTS", "CONSTRUCTOR", "CONTAINS", "CONTINUE", "CORRESPONDING", "CREATE", "CROSS", "CUBE",
				"CURRENT", "CURRENT_DATE", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
				"CURRENT_USER", "CURSOR", "CYCLE", "DATA", "DATALINK", "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL",
				"DECLARE", "DEFAULT", "DEFERRABLE", "DELETE", "DEPTH", "ASSERTION", "DEREF", "DESC", "DESCRIPTOR",
				"DIAGNOSTICS", "DICTIONARY", "DISCONNECT", "DO", "DOMAIN", "DOUBLE", "DROP", "ENDEXEC", "EQUALS",
				"ESCAPE", "EXCEPT", "EXCEPTION", "EXECUTE", "EXECUTE", "EXIT", "EXPAND", "EXPANDING", "FALSE", "FIRST",
				"FLOAT", "FOR", "FOREIGN", "FREE", "FROM", "FUNCTION", "GENERAL", "GET", "GLOBAL", "GOTO", "GROUP",
				"GROUPING", "HANDLER", "HASH", "HOUR", "IDENTITY", "IF", "IGNORE", "IMMEDIATE", "IN", "INDICATOR",
				"INITIALIZE", "INITIALLY", "INNER", "INOUT", "INPUT", "INSERT", "INT", "INTEGER", "INTERSECT",
				"INTERVAL", "INTO", "IS", "ISOLATION", "ITERATE", "JOIN", "KEY", "LANGUAGE", "LARGE", "LAST",
				"LATERAL", "LEADING", "LEAVE", "LEFT", "LESS", "LEVEL", "LIKE", "LIMIT", "LOCAL", "LOCALTIME",
				"LOCALTIMESTAMP", "LOCATOR", "LOOP", "MATCH", "MEETS", "MINUTE", "MODIFIES", "MODIFY", "MODULE",
				"MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NEXT", "NO", "NONE", "NORMALIZE",
				"NOT", "NULL", "NUMERIC", "OBJECT", "OF", "OFF", "OLD", "ON", "ONLY", "OPEN", "OPERATION", "OPTION",
				"OR", "ORDER", "ORDINALITY", "OUT", "OUTER", "USAGE", "USER", "USING", "VALUE", "VALUES", "VARIABLE",
				"VARYING", "VIEW", "WHEN", "WHENEVER", "WHERE", "WHILE", "WITH", "WRITE", "YEAR", "ZONE" };

		for (String word : words)
			keywords.add(word);
	}

	@Override
	public Result eval(String text, int position, ParserContext ctx) throws ParseException {
		int i = StringUtil.skipSpaces(text, position);

		int begin = i;

		if (text.length() <= begin)
			throw new BufferUnderflowException();

		char c = text.charAt(begin);
		if (!StringUtil.isAlpha(c) && c != '_')
			throw new ParseException("initial character must be alphabet", i);

		i = findEnd(text, i);
		String token = text.substring(begin, i);

		if (keywords.contains(token.toUpperCase()))
			throw new ParseException("reserved keyword", i);
		
		// remove trailing spaces
		i = StringUtil.skipSpaces(text, i);
		return new Result(new Binding(this, token), i);
	}

	private int findEnd(String text, int position) {
		int i = position;

		while (i < text.length() && (StringUtil.isAlphaNumeric(text.charAt(i)) || text.charAt(i) == '_'))
			i++;

		return i;
	}

	@Override
	public String toString() {
		return "regular_id";
	}
}

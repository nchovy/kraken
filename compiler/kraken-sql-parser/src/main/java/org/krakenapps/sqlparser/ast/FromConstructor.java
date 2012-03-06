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
package org.krakenapps.sqlparser.ast;

import java.util.List;

import org.krakenapps.sqlparser.impl.StringUtil;

public class FromConstructor implements InsertValues {
	private List<String> insertColumns;
	private List<ValueExpression> expressions;

	public FromConstructor(List<ValueExpression> expressions) {
		this(null, expressions);
	}

	public FromConstructor(List<String> insertColumns, List<ValueExpression> expressions) {
		this.insertColumns = insertColumns;
		this.expressions = expressions;
	}

	public List<String> getInsertColumns() {
		return insertColumns;
	}

	public List<ValueExpression> getRowValueExpressions() {
		return expressions;
	}

	@Override
	public String toString() {
		String columns = "";
		if (insertColumns != null)
			columns = "(" + StringUtil.join(", ", insertColumns) + ") ";
		return "FROM Constructor " + columns + expressions;
	}
}

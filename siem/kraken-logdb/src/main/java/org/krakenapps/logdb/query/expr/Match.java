/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logdb.query.expr;

import java.util.List;
import java.util.regex.Pattern;

import org.krakenapps.logdb.LogQueryCommand.LogMap;

public class Match implements Expression {

	private Expression valueExpr;
	private String pattern;
	private Pattern p;

	public Match(List<Expression> exprs) {
		this.valueExpr = exprs.get(0);
		this.pattern = (String) exprs.get(1).eval(null);
		p = Pattern.compile(pattern);
	}

	@Override
	public Object eval(LogMap map) {
		Object v = valueExpr.eval(map);
		if (v == null)
			return false;

		String s = v.toString();
		return p.matcher(s).find();
	}

	@Override
	public String toString() {
		return "match(" + valueExpr + ", " + pattern + ")";
	}
}

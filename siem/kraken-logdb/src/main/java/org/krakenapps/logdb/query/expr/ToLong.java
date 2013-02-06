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

import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.LogQueryCommand.LogMap;

public class ToLong implements Expression {
	private Expression valueExpr;

	// 10 for decimal (reserved extension)
	private int radix;

	public ToLong(List<Expression> exprs) {
		this.valueExpr = exprs.get(0);
		this.radix = 10;
		if (exprs.size() > 1)
			this.radix = (Integer) exprs.get(1).eval(null);

		if (radix != 10)
			throw new LogQueryParseException("invalid-argument", -1, "radix should be 10");
	}

	@Override
	public Object eval(LogMap map) {
		try {
			Object v = valueExpr.eval(map);
			if (v == null)
				return null;
			return Long.parseLong(v.toString(), radix);
		} catch (Throwable t) {
			return null;
		}
	}

	@Override
	public String toString() {
		return "long(" + valueExpr + ", " + radix + ")";
	}

}

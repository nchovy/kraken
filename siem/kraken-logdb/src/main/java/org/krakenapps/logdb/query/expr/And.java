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

import org.krakenapps.logdb.LogQueryCommand.LogMap;

public class And extends BinaryExpression {

	public And(Expression lhs, Expression rhs) {
		super(lhs, rhs);
	}

	@Override
	public Object eval(LogMap map) {
		Object lo = lhs.eval(map);
		Object ro = rhs.eval(map);

		boolean l = false;
		boolean r = false;

		if (lo instanceof Boolean)
			l = (Boolean) lo;
		else
			l = lo != null;

		if (ro instanceof Boolean)
			r = (Boolean) ro;
		else
			r = ro != null;

		return l && r;
	}

	@Override
	public String toString() {
		return "(" + lhs + " and " + rhs + ")";
	}
}

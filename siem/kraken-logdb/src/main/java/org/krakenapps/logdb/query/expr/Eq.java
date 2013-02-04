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
import org.krakenapps.logdb.query.ObjectComparator;

public class Eq extends BinaryExpression {
	private ObjectComparator cmp = new ObjectComparator();

	public Eq(Expression lhs, Expression rhs) {
		super(lhs, rhs);
	}

	@Override
	public Object eval(LogMap map) {
		return cmp.compare(lhs.eval(map), rhs.eval(map)) == 0;
	}

	@Override
	public String toString() {
		return "(" + lhs + " == " + rhs + ")";
	}
}

/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.logdb.query.command;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.query.expr.Expression;

public class Search extends LogQueryCommand {
	private Expression expr;

	public Search(Expression expr) {
		this.expr = expr;
	}

	public Expression getExpression() {
		return expr;
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void push(LogMap m) {
		boolean ret;
		Object o = expr.eval(m);
		if (o instanceof Boolean)
			ret = (Boolean) o;
		else
			ret = o != null;

		if (!ret)
			return;

		write(m);
	}

	@Override
	public boolean isReducer() {
		return false;
	}
}

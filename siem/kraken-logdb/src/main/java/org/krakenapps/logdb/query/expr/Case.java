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

import org.krakenapps.logdb.LogQueryCommand.LogMap;

public class Case implements Expression {
	private List<Expression> exprs;

	public Case(List<Expression> exprs) {
		this.exprs = exprs;
	}

	@Override
	public Object eval(LogMap map) {
		for (int i = 0; i < exprs.size();) {
			Expression cond = exprs.get(i++);
			Expression value = exprs.get(i++);

			Object condResult = cond.eval(map);
			if ((condResult instanceof Boolean)) {
				if ((Boolean) condResult)
					return value.eval(map);
			} else if (condResult != null)
				return value.eval(map);
		}

		return null;
	}

	@Override
	public String toString() {
		return "case(" + exprs + ")";
	}

}

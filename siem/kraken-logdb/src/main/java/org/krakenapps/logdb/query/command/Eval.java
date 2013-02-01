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
package org.krakenapps.logdb.query.command;

import org.krakenapps.logdb.LogQueryCommand;

public class Eval extends LogQueryCommand {
	private Term term;
	private String column;

	public Eval(Term term) {
		this(term, term.toString());
	}

	public Eval(Term term, String column) {
		this.term = term;
		this.column = column;
	}

	@Override
	public void push(LogMap m) {
		m.put(column, term.eval(m));
		write(m);
	}

	@Override
	public boolean isReducer() {
		return false;
	}
}

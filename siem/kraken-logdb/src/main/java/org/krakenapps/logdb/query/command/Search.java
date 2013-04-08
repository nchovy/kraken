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

import java.util.List;

import org.krakenapps.logdb.LogQueryCommand;

public class Search extends LogQueryCommand {
	private Integer limit;
	private Integer count;
	private List<Term> terms;

	public Search(List<Term> terms) {
		this(null, terms);
	}

	public Search(Integer limit, List<Term> terms) {
		this.limit = limit;
		this.terms = terms;
	}

	@Override
	public void init() {
		super.init();
		count = 0;
	}

	@Override
	public void push(LogMap m) {
		for (Term term : terms) {
			if (!term.eval(m))
				return;
		}

		write(m);

		if (limit != null && ++count == limit) {
			eof();
			return;
		}
	}

	@Override
	public boolean isReducer() {
		return false;
	}
}

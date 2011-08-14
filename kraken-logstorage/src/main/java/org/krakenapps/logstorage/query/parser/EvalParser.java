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
package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.StringPlaceholder;
import org.krakenapps.logstorage.query.command.Eval;
import org.krakenapps.logstorage.query.command.Eval.Term;
import org.krakenapps.logstorage.query.command.Eval.Term.Operator;

public class EvalParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		// @formatter:off
		syntax.add("eval", this, k("eval"), ref("option"), repeat(rule(new StringPlaceholder(), 
				choice(k("=="), k("!="), k(">"), k("<"), k(">="), k("<=")), 
				new StringPlaceholder(new char[] { ' ', ',' }))));
		// @formatter:on
		syntax.addRoot("eval");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		Map<String, String> option = (Map<String, String>) b.getChildren()[1].getValue();
		Integer limit = null;

		if (option.containsKey("limit"))
			limit = Integer.parseInt(option.get("limit"));

		List<Term> terms = new ArrayList<Eval.Term>();
		Binding[] c = b.getChildren();
		for (int i = 2; i < c.length; i++) {
			Binding[] v = c[i].getChildren();
			Term term = new Term();

			String lh = v[0].getValue().toString();
			term.setLh(lh);
			if (lh.startsWith("\"") && lh.endsWith("\"")) {
				term.setLhString(true);
				term.setLh(lh.substring(1, lh.length() - 1));
			}

			if (v[1].getValue().equals("=="))
				term.setOperator(Operator.Eq);
			else if (v[1].getValue().equals("!="))
				term.setOperator(Operator.Neq);
			else if (v[1].getValue().equals(">"))
				term.setOperator(Operator.Gt);
			else if (v[1].getValue().equals("<"))
				term.setOperator(Operator.Lt);
			else if (v[1].getValue().equals(">="))
				term.setOperator(Operator.Ge);
			else if (v[1].getValue().equals("<="))
				term.setOperator(Operator.Le);

			String rh = v[2].getValue().toString();
			term.setRh(rh);
			if (rh.startsWith("\"") && rh.endsWith("\"")) {
				term.setRhString(true);
				term.setRh(rh.substring(1, rh.length() - 1));
			}

			terms.add(term);
		}

		return new Eval(limit, terms);
	}
}

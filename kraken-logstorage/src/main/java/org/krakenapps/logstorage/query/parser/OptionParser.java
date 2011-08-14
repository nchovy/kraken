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

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.StringPlaceholder;

public class OptionParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("option", this, option(new StringPlaceholder(':'), new StringPlaceholder(), option(ref("option"))));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		if (b == null)
			return new HashMap<Object, Object>();

		Map<Object, Object> option = null;
		if (b.getChildren().length < 3)
			option = new HashMap<Object, Object>();
		else
			option = (Map<Object, Object>) b.getChildren()[2].getValue();
		option.put(b.getChildren()[0].getValue(), b.getChildren()[1].getValue());

		return option;
	}
}

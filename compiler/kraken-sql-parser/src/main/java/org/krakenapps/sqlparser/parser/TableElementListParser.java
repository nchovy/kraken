/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.sqlparser.parser;

import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.sqlparser.ast.TableElement;

public class TableElementListParser {
	private TableElementListParser() {
	}

	public static void parse(Binding root, List<TableElement> elements) {
		if (root.getValue() != null) {
			elements.add((TableElement) root.getValue());
			return;
		}

		TableElement value = (TableElement) root.getChildren()[0].getValue();
		elements.add(value);

		if (root.getChildren() != null)
			parse(root.getChildren()[2], elements);
	}

}

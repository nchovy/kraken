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
package org.krakenapps.logdb.pcap.impl;

import static org.krakenapps.bnf.Syntax.k;

import java.io.File;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;

public class PcapCommandParser implements LogQueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("pcap", this, k("pcap"), new StringPlaceholder());
		syntax.addRoot("pcap");
	}

	@Override
	public Object parse(Binding b) {
		String filename = (String) b.getChildren()[1].getValue();
		return new PcapCommand(new File(filename));
	}

}

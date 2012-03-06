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
package org.krakenapps.iptables.match;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class TcpMatchExtension implements MatchExtension {
	private Set<String> options;

	public TcpMatchExtension() {
		options = new HashSet<String>();
		options.add("spts");
		options.add("spt");
		options.add("dpts");
		options.add("dpt");
		options.add("flags");
	}

	@Override
	public String getName() {
		return "tcp";
	}

	@Override
	public MatchParseResult parse(String expr) {
		return parse(expr, 0, expr.length());
	}

	@Override
	public MatchParseResult parse(String expr, int begin, int end) {
		Scanner scanner = new Scanner(expr.substring(begin, end));
		TcpMatchOption o = new TcpMatchOption();

		int last = 0;
		while (scanner.hasNext()) {
			String token = scanner.next();

			String[] tokens = token.split(":");
			if (tokens == null)
				break;
			
			String type = tokens[0];
			if (!options.contains(type))
				break;

			if (type.equals("spts")) {
				int from = Integer.valueOf(tokens[1]);
				int to = Integer.valueOf(tokens[2]);
				PortRange range = new PortRange(from, to);
				o.setSourcePortRange(range);
			} else if (type.equals("spt")) {
				int from, to;
				from = to = Integer.valueOf(tokens[1]);
				PortRange range = new PortRange(from, to);
				o.setSourcePortRange(range);
			} else if (type.equals("dpts")) {
				int from = Integer.valueOf(tokens[1]);
				int to = Integer.valueOf(tokens[2]);
				PortRange range = new PortRange(from, to);
				o.setDestinationPortRange(range);
			} else if (type.equals("dpt")) {
				int from, to;
				from = to = Integer.valueOf(tokens[1]);
				PortRange range = new PortRange(from, to);
				o.setDestinationPortRange(range);
			} else if (type.equals("flags")) {
				// TODO:
			}

			last = begin + scanner.match().end();
		}

		return new MatchParseResult(o, last + 1);
	}

}

/*
 * Copyright 2011 NCHOVY
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

import java.util.Scanner;

public class IcmpMatchExtension implements MatchExtension {

	@Override
	public String getName() {
		return "icmp";
	}

	@Override
	public MatchParseResult parse(String expr) {
		return parse(expr, 0, expr.length());
	}

	@Override
	public MatchParseResult parse(String expr, int begin, int end) {
		Scanner scanner = new Scanner(expr.substring(begin, end));
		String token = scanner.next();
		Integer type = null;
		if (token.equals("type"))
			type = Integer.valueOf(scanner.next());
		
		int last = scanner.match().end();

		Integer code = null;
		if (scanner.hasNext()) {
			token = scanner.next();
			last = scanner.match().end(); 
				
			if (token.equals("code"))
				code = Integer.valueOf(scanner.next());
		}
		
		return new MatchParseResult(new IcmpMatchOption(type, code), last + 1);
	}

}

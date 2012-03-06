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
package org.krakenapps.iptables.target;

public class RejectTargetExtension implements TargetExtension {

	@Override
	public String getName() {
		return "REJECT";
	}

	@Override
	public TargetParseResult parse(String expr) {
		return parse(expr, 0, expr.length());
	}

	@Override
	public TargetParseResult parse(String expr, int begin, int end) {
		int space = end;
		int pos = expr.indexOf(' ', begin);
		if (pos > 0)
			space = pos;

		String type = expr.substring(begin, space);
		return new TargetParseResult(new RejectTargetOption(type), space + 1);
	}

}

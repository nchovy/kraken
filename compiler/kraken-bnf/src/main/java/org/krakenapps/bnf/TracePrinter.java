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
package org.krakenapps.bnf;

public class TracePrinter implements ParserTracer {
	private int depth = -2;

	@Override
	public ParserContext begin(String symbol, int position) {
		depth += 2;
		pad();
		System.out.println("-->begin parse: " + symbol + ", " + position);
		return null;
	}

	@Override
	public void error(String symbol, int position) {
		pad();
		System.out.println("<--error: " + symbol + ", " + position);
		depth -= 2;
	}

	@Override
	public void end(String symbol, int nextPosition) {
		pad();
		System.out.println("<--end parse: " + symbol + " " + nextPosition);
		depth -= 2;
	}

	@Override
	public void handle(Rule rule, String token) {
		pad();
		System.out.println(rule.getClass().getName() + ", " + token);
	}

	private void pad() {
		for (int i = 0; i < depth; i++)
			System.out.print(" ");
	}
}

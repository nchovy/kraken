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
package org.krakenapps.logdb.query.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.expr.EvalField;
import org.krakenapps.logdb.query.expr.EvalFunctions.Abs;
import org.krakenapps.logdb.query.expr.EvalFunctions.Min;
import org.krakenapps.logdb.query.expr.Expression;
import org.krakenapps.logdb.query.expr.NumberConstant;
import org.krakenapps.logdb.query.expr.NumericalExpressions;

public class ExpressionParser {

	public static Expression parse(String s) {
		List<Term> terms = tokenize(s);
		List<Term> output = convertToPostfix(terms);
		Stack<Expression> exprStack = new Stack<Expression>();
		for (Term term : output) {
			if (term instanceof OpTerm) {
				OpTerm op = (OpTerm) term;
				// is unary op?
				if (op.unary) {
					Expression expr = exprStack.pop();
					exprStack.add(new NumericalExpressions.Neg(expr));
					continue;
				}

				// reversed order by stack
				if (exprStack.size() < 2)
					throw new LogQueryParseException("broken-expression", -1);

				Expression rhs = exprStack.pop();
				Expression lhs = exprStack.pop();

				if (op == OpTerm.Add) {
					NumericalExpressions.Add add = new NumericalExpressions.Add(lhs, rhs);
					exprStack.add(add);
				} else if (op == OpTerm.Sub) {
					NumericalExpressions.Sub sub = new NumericalExpressions.Sub(lhs, rhs);
					exprStack.add(sub);
				} else if (op == OpTerm.Mul) {
					NumericalExpressions.Mul mul = new NumericalExpressions.Mul(lhs, rhs);
					exprStack.add(mul);
				} else if (op == OpTerm.Div) {
					NumericalExpressions.Div div = new NumericalExpressions.Div(lhs, rhs);
					exprStack.add(div);
				}

			} else if (term instanceof TokenTerm) {
				// parse token expression (variable or numeric constant)
				TokenTerm t = (TokenTerm) term;
				if (!t.text.equals("(") && !t.text.equals(")")) {
					String token = ((TokenTerm) term).text.trim();
					Expression expr = parseTokenExpr(exprStack, token);
					exprStack.add(expr);
				}
			} else {
				// parse function expression
				FuncTerm f = (FuncTerm) term;
				String name = f.tokens.remove(0);
				List<Expression> args = parseArgs(f.tokens);

				if (name.equals("abs")) {
					exprStack.add(new Abs(args.get(0)));
				} else if (name.equals("min")) {
					exprStack.add(new Min(args));
				}
			}
		}

		return exprStack.pop();
	}

	private static Expression parseTokenExpr(Stack<Expression> exprStack, String token) {
		try {
			long v = Long.parseLong(token);
			return new NumberConstant(v);
		} catch (NumberFormatException e1) {
			try {
				double v = Double.parseDouble(token);
				return new NumberConstant(v);
			} catch (NumberFormatException e2) {
				return new EvalField(token);
			}
		}
	}

	private static List<Expression> parseArgs(List<String> tokens) {
		// separate by outermost comma (not in nested function call)
		List<Expression> exprs = new ArrayList<Expression>();

		int parensCount = 0;

		List<String> subTokens = new ArrayList<String>();
		tokens = tokens.subList(1, tokens.size() - 1);

		for (String token : tokens) {
			String t = token.trim();
			if (t.equals("("))
				parensCount++;
			if (t.equals(")"))
				parensCount--;

			if (parensCount == 0 && t.equals(",")) {
				exprs.add(parseArg(subTokens));
				subTokens = new ArrayList<String>();
			} else
				subTokens.add(token);
		}

		exprs.add(parseArg(subTokens));
		return exprs;
	}

	private static Expression parseArg(List<String> tokens) {
		StringBuilder sb = new StringBuilder();
		for (String token : tokens) {
			sb.append(token);
		}

		return parse(sb.toString());
	}

	private static List<Term> convertToPostfix(List<Term> tokens) {
		Stack<Term> opStack = new Stack<Term>();
		List<Term> output = new ArrayList<Term>();

		int i = 0;
		int len = tokens.size();
		while (i < len) {
			Term token = tokens.get(i);

			if (isDelimiter(token)) {
				// need to pop operator and write to output?
				if (needPop(token, opStack, output)) {
					Term last = opStack.pop();
					output.add(last);
				}

				if (token instanceof OpTerm) {
					opStack.add(token);
				} else if (((TokenTerm) token).text.equals("(")) {
					opStack.add(token);
				} else if (((TokenTerm) token).text.equals(")")) {
					boolean foundMatchParens = false;
					while (!opStack.isEmpty()) {
						Term last = opStack.pop();
						if (last instanceof TokenTerm && ((TokenTerm) last).text.equals("(")) {
							foundMatchParens = true;
							break;
						} else {
							output.add(last);
						}
					}

					if (!foundMatchParens)
						throw new LogQueryParseException("parens-mismatch", -1);
				}
			} else {
				output.add(token);
			}

			i++;
		}

		// last operator flush
		while (!opStack.isEmpty()) {
			Term op = opStack.pop();
			output.add(op);
		}

		return output;
	}

	private static boolean needPop(Term token, Stack<Term> opStack, List<Term> output) {
		if (!(token instanceof OpTerm))
			return false;

		OpTerm currentOp = (OpTerm) token;
		OpTerm lastOp = null;
		if (!opStack.isEmpty()) {
			Term t = opStack.peek();
			if (!(t instanceof OpTerm))
				return false;
			lastOp = (OpTerm) t;
		}

		if (lastOp == null)
			return false;

		int precedence = currentOp.precedence;
		int lastPrecedence = lastOp.precedence;

		if (currentOp.leftAssoc && precedence <= lastPrecedence)
			return true;

		if (precedence < lastPrecedence)
			return true;

		return false;
	}

	private static boolean isOperator(String token) {
		if (token == null)
			return false;
		return isDelimiter(token.charAt(0));
	}

	public static List<Term> tokenize(String s) {
		return tokenize(s, 0, s.length() - 1);
	}

	public static List<Term> tokenize(String s, int begin, int end) {
		List<Term> tokens = new ArrayList<Term>();

		String lastToken = null;
		int next = begin;
		while (true) {
			ParseResult r = nextToken(s, next, end);
			if (r == null)
				break;

			String token = (String) r.value;

			// read function call (including nested one)
			int parenCount = 0;
			List<String> functionTokens = new ArrayList<String>();
			if (token.equals("(") && lastToken != null && !isOperator(lastToken)) {
				functionTokens.add(lastToken);

				while (true) {
					ParseResult r2 = nextToken(s, next, end);
					if (r2 == null) {
						break;
					}

					String funcToken = (String) r2.value;
					functionTokens.add(funcToken);

					if (funcToken.equals("("))
						parenCount++;

					if (funcToken.equals(")")) {
						parenCount--;
					}

					if (parenCount == 0) {
						r.next = r2.next;
						break;
					}

					next = r2.next;
				}
			}

			OpTerm op = OpTerm.parse(token);

			// check if unary operator
			if (op != null && op.symbol.equals("-") && (lastToken == null || lastToken.equals("("))) {
				op = OpTerm.Neg;
			}

			if (functionTokens.size() > 0) {
				// remove last term and add function term instead
				tokens.remove(tokens.size() - 1);
				tokens.add(new FuncTerm(functionTokens));
			} else if (op != null)
				tokens.add(op);
			else
				tokens.add(new TokenTerm(token));

			next = r.next;
			lastToken = token;
		}

		return tokens;
	}

	private static ParseResult nextToken(String s, int begin, int end) {
		if (begin > end)
			return null;

		begin = skipSpaces(s, begin);
		int p = findNextDelimiter(s, begin, end);
		if (p < begin) {
			// no operator, return whole string
			String token = s.substring(begin, end + 1);
			return new ParseResult(token, end + 1);
		} else if (p == begin) {
			// return operator
			char c = s.charAt(p);
			return new ParseResult(Character.toString(c), p + 1);
		} else {
			// return term
			String token = s.substring(begin, p);
			return new ParseResult(token, p);
		}
	}

	private static int findNextDelimiter(String s, int begin, int end) {
		for (int i = begin; i <= end; i++) {
			char c = s.charAt(i);
			if (isDelimiter(c))
				return i;
		}

		return -1;
	}

	private static boolean isDelimiter(Term t) {
		if (t instanceof OpTerm)
			return true;

		if (t instanceof TokenTerm) {
			String text = ((TokenTerm) t).text;
			return text.equals("(") || text.equals(")");
		}

		return false;
	}

	private static boolean isDelimiter(char c) {
		return c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')' || c == ',';
	}

	private static interface Term {
	}

	private static class TokenTerm implements Term {
		private String text;

		public TokenTerm(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	private static enum OpTerm implements Term {
		Add("+", 2), Sub("-", 2), Mul("*", 3), Div("/", 3), Neg("-", 4, false, true);

		OpTerm(String symbol, int precedence) {
			this(symbol, precedence, true, false);
		}

		OpTerm(String symbol, int precedence, boolean leftAssoc, boolean unary) {
			this.symbol = symbol;
			this.precedence = precedence;
			this.leftAssoc = leftAssoc;
			this.unary = unary;
		}

		public String symbol;
		public int precedence;
		public boolean leftAssoc;
		public boolean unary;

		public static OpTerm parse(String token) {
			for (OpTerm t : values())
				if (t.symbol.equals(token))
					return t;

			return null;
		}

		@Override
		public String toString() {
			return symbol;
		}
	}

	private static class FuncTerm implements Term {
		private List<String> tokens;

		public FuncTerm(List<String> tokens) {
			this.tokens = tokens;
		}

		@Override
		public String toString() {
			return "func term(" + tokens + ")";
		}
	}

	public static int skipSpaces(String text, int position) {
		int i = position;

		while (i < text.length() && text.charAt(i) == ' ')
			i++;

		return i;
	}
}

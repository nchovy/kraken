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
import org.krakenapps.logdb.query.expr.Abs;
import org.krakenapps.logdb.query.expr.Add;
import org.krakenapps.logdb.query.expr.And;
import org.krakenapps.logdb.query.expr.Case;
import org.krakenapps.logdb.query.expr.Concat;
import org.krakenapps.logdb.query.expr.Div;
import org.krakenapps.logdb.query.expr.Eq;
import org.krakenapps.logdb.query.expr.EvalField;
import org.krakenapps.logdb.query.expr.Expression;
import org.krakenapps.logdb.query.expr.Gt;
import org.krakenapps.logdb.query.expr.Gte;
import org.krakenapps.logdb.query.expr.If;
import org.krakenapps.logdb.query.expr.Left;
import org.krakenapps.logdb.query.expr.Len;
import org.krakenapps.logdb.query.expr.Lt;
import org.krakenapps.logdb.query.expr.Lte;
import org.krakenapps.logdb.query.expr.Min;
import org.krakenapps.logdb.query.expr.Mul;
import org.krakenapps.logdb.query.expr.Neg;
import org.krakenapps.logdb.query.expr.Neq;
import org.krakenapps.logdb.query.expr.NumberConstant;
import org.krakenapps.logdb.query.expr.Or;
import org.krakenapps.logdb.query.expr.Right;
import org.krakenapps.logdb.query.expr.StringConstant;
import org.krakenapps.logdb.query.expr.Sub;
import org.krakenapps.logdb.query.expr.Substr;
import org.krakenapps.logdb.query.expr.ToDate;
import org.krakenapps.logdb.query.expr.ToInt;
import org.krakenapps.logdb.query.expr.ToLong;
import org.krakenapps.logdb.query.expr.ToString;
import org.krakenapps.logdb.query.expr.Trim;

public class ExpressionParser {

	public static Expression parse(String s) {
		System.out.println("parse - " + s);
		List<Term> terms = tokenize(s);
		List<Term> output = convertToPostfix(terms);
		Stack<Expression> exprStack = new Stack<Expression>();
		for (Term term : output) {
			if (term instanceof OpTerm) {
				OpTerm op = (OpTerm) term;
				// is unary op?
				if (op.unary) {
					Expression expr = exprStack.pop();
					exprStack.add(new Neg(expr));
					continue;
				}

				// reversed order by stack
				if (exprStack.size() < 2)
					throw new LogQueryParseException("broken-expression", -1, "operator is [" + op + "]");

				Expression rhs = exprStack.pop();
				Expression lhs = exprStack.pop();

				switch (op) {
				case Add:
					exprStack.add(new Add(lhs, rhs));
					break;
				case Sub:
					exprStack.add(new Sub(lhs, rhs));
					break;
				case Mul:
					exprStack.add(new Mul(lhs, rhs));
					break;
				case Div:
					exprStack.add(new Div(lhs, rhs));
					break;
				case Gte:
					exprStack.add(new Gte(lhs, rhs));
					break;
				case Lte:
					exprStack.add(new Lte(lhs, rhs));
					break;
				case Lt:
					exprStack.add(new Lt(lhs, rhs));
					break;
				case Gt:
					exprStack.add(new Gt(lhs, rhs));
					break;
				case And:
					exprStack.add(new And(lhs, rhs));
					break;
				case Or:
					exprStack.add(new Or(lhs, rhs));
					break;
				case Eq:
					exprStack.add(new Eq(lhs, rhs));
					break;
				case Neq:
					exprStack.add(new Neq(lhs, rhs));
					break;
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
				String name = f.tokens.remove(0).trim();
				List<Expression> args = parseArgs(f.tokens);

				if (name.equals("abs")) {
					exprStack.add(new Abs(args.get(0)));
				} else if (name.equals("min")) {
					exprStack.add(new Min(args));
				} else if (name.equals("case")) {
					exprStack.add(new Case(args));
				} else if (name.equals("if")) {
					exprStack.add(new If(args));
				} else if (name.equals("concat")) {
					exprStack.add(new Concat(args));
				} else if (name.equals("str")) {
					exprStack.add(new ToString(args));
				} else if (name.equals("long")) {
					exprStack.add(new ToLong(args));
				} else if (name.equals("int")) {
					exprStack.add(new ToInt(args));
				} else if (name.equals("date")) {
					exprStack.add(new ToDate(args));
				} else if (name.equals("string")) {
					exprStack.add(new ToString(args));
				} else if (name.equals("left")) {
					exprStack.add(new Left(args));
				} else if (name.equals("right")) {
					exprStack.add(new Right(args));
				} else if (name.equals("trim")) {
					exprStack.add(new Trim(args));
				} else if (name.equals("len")) {
					exprStack.add(new Len(args));
				} else if (name.equals("substr")) {
					exprStack.add(new Substr(args));
				} else {
					throw new LogQueryParseException("unsupported-function", -1, "function name is " + name);
				}
			}
		}

		return exprStack.pop();
	}

	private static Expression parseTokenExpr(Stack<Expression> exprStack, String token) {
		// is quoted?
		if (token.startsWith("\"") && token.endsWith("\""))
			return new StringConstant(token.substring(1, token.length() - 1));

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
				while (needPop(token, opStack, output)) {
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
		return isDelimiter(token);
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

		// use r.next as a position here (need +1 for actual next)
		ParseResult r = findNextDelimiter(s, begin, end);
		if (r.next < begin) {
			// no operator, return whole string
			String token = s.substring(begin, end + 1);
			return new ParseResult(token, end + 1);
		} else if (r.next == begin) {
			// check if next token is quoted string
			if (r.value.equals("\"")) {
				int p = s.indexOf('"', r.next + 1);
				if (p < 0) {
					String quoted = s.substring(r.next);
					return new ParseResult(quoted, s.length());
				} else {
					String quoted = s.substring(r.next, p + 1);
					return new ParseResult(quoted, p + 1);
				}
			}

			// return operator
			int len = ((String) r.value).length();
			return new ParseResult((String) r.value, r.next + len);
		} else {
			// return term
			String token = s.substring(begin, r.next);
			if (!token.trim().isEmpty())
				return new ParseResult(token, r.next);
			else {
				return nextToken(s, skipSpaces(s, begin), end);
			}
		}
	}

	private static ParseResult findNextDelimiter(String s, int begin, int end) {
		// check parens, comma and operators
		ParseResult r = new ParseResult(null, -1);
		min(r, "\"", s.indexOf('"', begin), end);
		min(r, "(", s.indexOf('(', begin), end);
		min(r, ")", s.indexOf(')', begin), end);
		min(r, ",", s.indexOf(',', begin), end);
		for (OpTerm op : OpTerm.values()) {
			min(r, op.symbol, s.indexOf(op.symbol, begin), end);
		}

		return r;
	}

	private static boolean isDelimiter(String s) {
		String d = s.trim();

		if (d.equals("(") || d.equals(")") || d.equals(","))
			return true;

		for (OpTerm op : OpTerm.values())
			if (op.symbol.equals(s))
				return true;

		return false;
	}

	private static void min(ParseResult r, String symbol, int p, int end) {
		if (p < 0)
			return;

		boolean change = p >= 0 && p <= end && (r.next == -1 || p < r.next);
		if (change) {
			r.value = symbol;
			r.next = p;
		}
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
		Add("+", 3), Sub("-", 3), Mul("*", 4), Div("/", 4), Neg("-", 5, false, true), Gte(">=", 2), Lte("<=", 2), Gt(">", 2), Lt(
				"<", 2), Eq("==", 1), Neq("!=", 1), IsNull(" is null", 1, true, true), And(" and ", 0), Or(" or ", 0);

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

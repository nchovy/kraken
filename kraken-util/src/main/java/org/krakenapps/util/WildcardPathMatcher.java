package org.krakenapps.util;

import java.util.regex.Pattern;

public class WildcardPathMatcher {
	private Pattern pattern;

	public WildcardPathMatcher(String token) {
		pattern = Pattern.compile(wildcardToRegex(token));
	}

	private String wildcardToRegex(String token) {
		StringBuffer buf = new StringBuffer(token.length());
		buf.append("^");
		for (int i = 0, is = token.length(); i < is; ++i) {
			char ch = token.charAt(i);
			switch (ch) {
			case '*':
				// buf.append(".*?");
				buf.append("[^\\/\\\\]*?");
				break;
			case '\\':
			case '.':
			case '[':
			case ']':
			case '(':
			case ')':
			case '$':
			case '^':
			case '{':
			case '}':
			case '|':
				buf.append("\\");
				buf.append(ch);
				break;
			default:
				buf.append(ch);
				break;
			}
		}
		buf.append("$");
		return buf.toString();
	}

	public boolean isMatch(String name) {
		return pattern.matcher(name).matches();
	}

}
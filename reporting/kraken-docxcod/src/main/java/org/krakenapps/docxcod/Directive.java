package org.krakenapps.docxcod;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

public class Directive {

	private final Node position;
	private final String directiveStr;

	public Directive(Node parentNode, String extractDirective) {
		this.position = parentNode;
		this.directiveStr = extractDirective;
	}

	public Node getPosition() {
		return position;
	}

	public String getDirectiveString() {
		return directiveStr;
	}

	@SuppressWarnings("unused")
	private static Pattern quotedString = Pattern
			.compile("[^\\s\"]*\"([^\\\\\"]+|\\\\([btnfr\"'\\\\]|[0-3]?[0-7]{1,2}|u[0-9a-fA-F]{4}))*\"[^\\s\"]*");
	private static Pattern MERGEFIELD_PATTERN = Pattern.compile("MERGEFIELD\\s+(?:\"(.*)\"|([^\"].*[^\"]))");

	private static String parseMergeField(String in) {
		in = replaceUnicodeQuote(in.trim());
		Matcher matcher = MERGEFIELD_PATTERN.matcher(in);
		if (matcher.find() && matcher.groupCount() > 0) {
			String f = matcher.group(1);
			if (f == null)
				f = matcher.group(2);
			f = f.replaceAll("\\s*\\\\\\*\\s*MERGEFORMAT\\s*", "");
			f = f.replaceAll("\\\\(.)", "$1");
			return f;
		} else
			return null;
	}

	private static String replaceUnicodeQuote(String in) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < in.length(); ++i) {
			int type = Character.getType(in.codePointAt(i));
			switch (type) {
			case Character.FINAL_QUOTE_PUNCTUATION:
			case Character.INITIAL_QUOTE_PUNCTUATION:
				builder.append('"');
				break;
			default:
				builder.append(in.charAt(i));
				break;
			}
		}
		return builder.toString();
	}

	public static String extractDirective(String nodeValue) {
		String result = nodeValue.trim();
		if (result.startsWith("MERGEFIELD")) {
			result = parseMergeField(result);
		}
		return result;
	}

}

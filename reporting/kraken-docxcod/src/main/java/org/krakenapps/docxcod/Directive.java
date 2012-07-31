package org.krakenapps.docxcod;

import java.util.Scanner;
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
	
	private static Pattern quotedString = Pattern.compile("\"([^\\\\\"]+|\\\\([btnfr\"'\\\\]|[0-3]?[0-7]{1,2}|u[0-9a-fA-F]{4}))*\""); 
	
	private static String parseMergeField(String in) {
		in = replaceUnicodeQuote(in.trim());
		Scanner sc = new Scanner(in);
		sc.hasNext("MERGEFIELD");
		String f = sc.findInLine(quotedString);
		if (f != null) {
			f = f.substring(1, f.length() - 1);
			f = f.replaceAll("\\\\(.)", "$1");
			return f;
		} else {
			return null;
		}
	}
	
	private static String replaceUnicodeQuote(String in) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < in.length(); ++i) {
			int type = Character.getType(in.codePointAt(i));
			switch(type) {
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

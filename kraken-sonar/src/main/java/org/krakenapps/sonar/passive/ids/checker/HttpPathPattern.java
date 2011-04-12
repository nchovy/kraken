package org.krakenapps.sonar.passive.ids.checker;

import java.nio.charset.Charset;

import org.krakenapps.ahocorasick.Pattern;
import org.krakenapps.sonar.passive.ids.rule.Rule;

public class HttpPathPattern implements Pattern {
	private byte[] keyword;
	private Rule rule;
	
	public HttpPathPattern(Rule rule) {
		this.keyword = rule.find("path").getBytes(Charset.forName("utf-8"));
		this.rule = rule;
	}
	@Override
	public byte[] getKeyword() {
		return keyword;
	}
	
	public Rule getRule() {
		return rule;
	}
}

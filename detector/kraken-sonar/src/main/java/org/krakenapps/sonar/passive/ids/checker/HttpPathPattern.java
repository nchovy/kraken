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

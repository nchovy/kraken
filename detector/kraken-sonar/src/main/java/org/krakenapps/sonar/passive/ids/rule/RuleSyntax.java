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
 package org.krakenapps.sonar.passive.ids.rule;

import java.text.ParseException;

import org.krakenapps.bnf.Syntax;

import static org.krakenapps.bnf.Syntax.*;

public class RuleSyntax {
	private Syntax syntax;

	public RuleSyntax() {
		syntax = new Syntax();
		syntax.addRoot("rule");
		syntax.add("rule", new RuleParser(), t("("), ref("body"), t(")"));
		syntax.add("body", new RuleBodyParser(), repeat(rule(ref("option"), option(t(";")))));
		syntax.add("option", new RuleOptionParser(), new OptionNamePlaceholder(), t(":"), new OptionValuePlaceholder());
	}

	public Rule eval(String text) throws ParseException {
		return (Rule) syntax.eval(text);
	}

	public static void main(String[] args) throws ParseException {
		RuleSyntax s = new RuleSyntax();
		Rule r = s.eval("(type: http; id: NCHOVY-2010-0001; name: PHP-Nuke <= 7.9 Final (phpbb_root_path); path: /modules/Forums/admin/admin_words.php; check-rfi: phpbb_root_path; reference: http://www.exploit-db.com/exploits/1866/; cve:CVE-2006-2828;)");
		System.out.println(r);
	}
}

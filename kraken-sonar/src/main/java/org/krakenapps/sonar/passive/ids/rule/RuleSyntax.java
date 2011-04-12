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

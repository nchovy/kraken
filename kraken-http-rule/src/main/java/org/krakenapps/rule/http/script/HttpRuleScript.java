/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.rule.http.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.ahocorasick.AhoCorasickSearch;
import org.krakenapps.ahocorasick.Pair;
import org.krakenapps.ahocorasick.Pattern;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.rule.Rule;
import org.krakenapps.rule.http.HttpRequestContext;
import org.krakenapps.rule.http.HttpRequestRule;
import org.krakenapps.rule.http.HttpRuleEngine;
import org.krakenapps.rule.http.LocalFileInclusionRule;
import org.krakenapps.rule.http.RemoteFileInclusionRule;
import org.krakenapps.rule.http.URLParser;
import org.krakenapps.rule.http.VariableRegexRule;
import org.krakenapps.rule.parser.GenericRule;
import org.krakenapps.rule.parser.GenericRuleOption;
import org.krakenapps.rule.parser.GenericRuleSyntax;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRuleScript implements Script {
	private Logger logger = LoggerFactory.getLogger(HttpRuleScript.class);
	private HttpRuleEngine engine;
	private ScriptContext context;

	public HttpRuleScript(HttpRuleEngine engine) {
		this.engine = engine;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void reload(String[] args) {
		engine.reload();
		context.println("reloaded rules");
	}

	@ScriptUsage(description = "inspect http uri and test attack detection", arguments = { @ScriptArgument(name = "url", type = "string", description = "path + querystring") })
	public void inspect(String[] args) {
		HttpRequestContext c = URLParser.parse("GET", args[0]);

		HttpRequestRule match = engine.match(c);
		if (match != null) {
			context.println(match.toString());
		} else {
			context.println("no attack found");
		}
	}

	public void test(String[] args) {
		for (String s : args)
			context.println(s);
	}

	@ScriptUsage(description = "inspect http uri and test attack detection", arguments = { @ScriptArgument(name = "url", type = "string", description = "path + querystring") })
	public void ruleTest(String[] args) {
		context.println("Ctrl-C to quit.");
		try {
			while (true) {
				context.print("rule: ");
				String rule = context.readLine();
				rule = rule.trim();
				if (rule.isEmpty() || rule.startsWith(";"))
					continue;
				AhoCorasickSearch acs = compile(rule);

				for (String url : args) {
					if (ruleTest(acs, url))
						context.println("catch : " + url);
					else
						context.println("no attack found : " + url);
				}
			}
		} catch (InterruptedException e) {
			context.println("interrupted.");
		} catch (Exception e) {
			context.println(e);
			logger.error("kraken-http-rule: rule test error.", e);
		}
	}

	private boolean ruleTest(AhoCorasickSearch acs, String url) throws Exception {
		HttpRequestContext c = URLParser.parse("GET", url);
		byte[] b = c.getPath().getBytes("utf-8");
		List<Pair> pairs = acs.search(b);

		for (Pair p : pairs) {
			Rule rule = ((RulePattern) p.getPattern()).getRule();
			HttpRequestRule httpRule = (HttpRequestRule) rule;
			if (httpRule.match(c))
				return true;
		}

		return false;
	}

	private AhoCorasickSearch compile(String ruleString) throws Exception {
		AhoCorasickSearch acs = new AhoCorasickSearch();
		GenericRule r = new GenericRuleSyntax().eval(ruleString);
		Rule rule = null;

		String type = r.get("type");
		String path = r.get("path");

		if (type.equals("rfi")) {
			String var = r.get("var");
			rule = new RemoteFileInclusionRule(r.getId(), r.getMessage(), path, var);
		} else if (type.equals("lfi")) {
			Map<String, String> params = new HashMap<String, String>();

			String name = null;
			for (GenericRuleOption o : r.getOptions()) {
				if (o.getName().equals("var")) {
					if (name != null)
						params.put(name, null);

					name = o.getValue();
				} else if (name != null && o.getName().equals("value")) {
					params.put(name, o.getValue());
					name = null;
				}
			}

			rule = new LocalFileInclusionRule(r.getId(), r.getMessage(), path, params);
		} else if (type.equals("regex")) {
			String var = r.get("var");
			String regex = r.get("regex");
			rule = new VariableRegexRule(r.getId(), r.getMessage(), path, var, regex);
		}
		if (rule != null) {
			rule.getReferences().addAll(convert(r.getAll("reference")));
			rule.getCveNames().addAll(r.getAll("cve"));

			acs.addKeyword(new RulePattern(path, rule));
		}
		acs.compile();

		return acs;
	}

	private Collection<URL> convert(Collection<String> references) {
		List<URL> urls = new ArrayList<URL>();
		for (String reference : references) {
			try {
				urls.add(new URL(reference));
			} catch (MalformedURLException e) {
			}
		}
		return urls;
	}

	private static class RulePattern implements Pattern {
		private String s;
		private Rule r;

		public RulePattern(String s, Rule r) {
			this.s = s;
			this.r = r;
		}

		public Rule getRule() {
			return r;
		}

		@Override
		public byte[] getKeyword() {
			try {
				return s.getBytes("utf-8");
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		}

		@Override
		public String toString() {
			return r.toString();
		}
	}

	@ScriptUsage(description = "list all request rules", arguments = { @ScriptArgument(name = "id", type = "string", description = "rule id", optional = true) })
	public void requestRules(String[] args) {
		context.println("HTTP Request Rules");
		context.println("----------------------");
		Collection<HttpRequestRule> requestRules = null;

		if (args.length > 0) {
			requestRules = engine.getRequestRules(args[0]);
		} else {
			requestRules = engine.getRequestRules();
		}

		for (HttpRequestRule r : requestRules) {
			context.println(r.toString());
		}
	}

	@ScriptUsage(description = "import rule file", arguments = { @ScriptArgument(name = "file path", type = "string", description = "rule file path") })
	public void importRuleFiles(String[] args) {
		for (String path : args) {
			File source = new File(path);

			if (!source.exists()) {
				context.println(path + " not found.");
				continue;
			}

			File destination = null;
			for (int i = 0;; i++) {
				StringBuilder newPath = new StringBuilder();
				String filename = source.getName();
				int ext = filename.contains(".") ? filename.lastIndexOf(".") : filename.length();

				newPath.append("data/kraken-http-rule/");
				newPath.append(filename.substring(0, ext));
				if (i > 0)
					newPath.append(" (" + i + ")");
				newPath.append(filename.substring(ext));
				destination = new File(newPath.toString());
				if (!destination.exists())
					break;
			}

			FileChannel fcin = null;
			FileChannel fcout = null;
			try {
				destination.createNewFile();

				fcin = new FileInputStream(source).getChannel();
				fcout = new FileOutputStream(destination).getChannel();
				fcin.transferTo(0, fcin.size(), fcout);

				context.println("import successed.");
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				context.println("import failed.");
			} finally {
				try {
					if (fcin != null)
						fcin.close();
				} catch (IOException e) {
				}

				try {
					if (fcout != null)
						fcout.close();
				} catch (IOException e) {
				}
			}
		}
	}
}

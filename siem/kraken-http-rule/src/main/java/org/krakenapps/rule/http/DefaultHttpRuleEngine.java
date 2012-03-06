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
package org.krakenapps.rule.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.ahocorasick.AhoCorasickSearch;
import org.krakenapps.ahocorasick.Pair;
import org.krakenapps.ahocorasick.Pattern;
import org.krakenapps.ahocorasick.SearchContext;
import org.krakenapps.rule.Rule;
import org.krakenapps.rule.RuleDatabase;
import org.krakenapps.rule.RuleGroup;
import org.krakenapps.rule.http.VariableRegexRule.ParameterValue;
import org.krakenapps.rule.parser.GenericRule;
import org.krakenapps.rule.parser.GenericRuleOption;
import org.krakenapps.rule.parser.GenericRuleSyntax;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "http-rule-engine")
@Provides
public class DefaultHttpRuleEngine implements HttpRuleEngine {
	private final Logger logger = LoggerFactory.getLogger(DefaultHttpRuleEngine.class.getName());
	private BundleContext bc;

	private GenericRuleSyntax syntax = new GenericRuleSyntax();

	private volatile AhoCorasickSearch acm = null;
	private volatile Map<String, List<HttpRequestRule>> requestRuleMap = new ConcurrentHashMap<String, List<HttpRequestRule>>();
	private volatile Map<String, List<HttpResponseRule>> responseRuleMap = new ConcurrentHashMap<String, List<HttpResponseRule>>();

	public DefaultHttpRuleEngine(BundleContext bc) {
		this.bc = bc;
	}

	@Override
	public String getName() {
		return "http";
	}

	@Override
	public String getDescription() {
		return "Default http rule engine";
	}

	@Override
	public Collection<Rule> getRules() {
		List<Rule> rules = new LinkedList<Rule>();

		for (List<HttpRequestRule> r : requestRuleMap.values())
			rules.addAll(r);

		for (List<HttpResponseRule> r : responseRuleMap.values())
			rules.addAll(r);

		return rules;
	}

	@Override
	public void reload() {
		File dir = new File(System.getProperty("kraken.data.dir"), "kraken-http-rule");
		dir.mkdirs();

		AhoCorasickSearch s = new AhoCorasickSearch();
		Map<String, List<HttpRequestRule>> reqs = new ConcurrentHashMap<String, List<HttpRequestRule>>();
		Map<String, List<HttpResponseRule>> resps = new ConcurrentHashMap<String, List<HttpResponseRule>>();

		File[] files = dir.listFiles(new RuleFileFilter());
		for (File f : files) {
			loadRules(s, reqs, resps, f);
		}

		RuleDatabase ruleDb = getRuleDatabase();
		if (ruleDb != null) {
			for (RuleGroup group : ruleDb.getRuleGroups("http")) {
				for (Rule rule : group.getRules()) {
					try {
						parse(s, reqs, resps, rule.toString());
					} catch (ParseException e) {
					}
				}
			}
		}

		s.compile();

		this.acm = s;
		this.requestRuleMap = reqs;
		this.responseRuleMap = resps;
	}

	private RuleDatabase getRuleDatabase() {
		String className = RuleDatabase.class.getName();
		ServiceReference ref = bc.getServiceReference(className);
		if (ref == null)
			return null;

		return (RuleDatabase) bc.getService(ref);
	}

	@Validate
	public void start() {
		reload();
	}

	private void loadRules(AhoCorasickSearch fsm, Map<String, List<HttpRequestRule>> reqs,
			Map<String, List<HttpResponseRule>> resps, File f) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				parse(fsm, reqs, resps, line);
			}
		} catch (Exception e) {
			logger.error("kraken http rule: cannot open http-rule file", e);
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}
	}

	private void parse(AhoCorasickSearch fsm, Map<String, List<HttpRequestRule>> reqs,
			Map<String, List<HttpResponseRule>> resps, String line) throws ParseException {
		line = line.trim();
		if (line.isEmpty() || line.startsWith(";"))
			return;

		GenericRule r = syntax.eval(line);
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

			if (name != null)
				params.put(name, null);

			rule = new LocalFileInclusionRule(r.getId(), r.getMessage(), path, params);
		} else if (type.equals("regex")) {
			Map<String, ParameterValue> params = new HashMap<String, VariableRegexRule.ParameterValue>();

			String name = null;
			for (GenericRuleOption o : r.getOptions()) {
				if (o.getName().equals("var")) {
					if (name != null)
						params.put(name, null);

					name = o.getValue();
				} else if (name != null && o.getName().equals("value")) {
					params.put(name, new ParameterValue(o.getValue()));
					name = null;
				} else if (name != null && o.getName().equals("regex")) {
					params.put(name, new ParameterValue(o.getValue(), true));
					name = null;
				}
			}

			if (name != null)
				params.put(name, null);

			rule = new VariableRegexRule(r.getId(), r.getMessage(), path, params);
		}

		try {
			if (rule != null) {
				rule.getReferences().addAll(convert(r.getAll("reference")));
				rule.getCveNames().addAll(r.getAll("cve"));

				fsm.addKeyword(new RulePattern(path, rule));

				if (!reqs.containsKey(rule.getId()))
					reqs.put(rule.getId(), new ArrayList<HttpRequestRule>());

				reqs.get(rule.getId()).add((HttpRequestRule) rule);
			}
		} catch (UnsupportedEncodingException e) {
		}
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

	@Override
	public Collection<HttpRequestRule> getRequestRules() {
		List<HttpRequestRule> rules = new LinkedList<HttpRequestRule>();
		for (List<HttpRequestRule> r : requestRuleMap.values()) {
			rules.addAll(r);
		}
		return rules;
	}

	@Override
	public Collection<HttpRequestRule> getRequestRules(String id) {
		return requestRuleMap.get(id);
	}

	@Override
	public Collection<HttpResponseRule> getResponseRules() {
		List<HttpResponseRule> rules = new LinkedList<HttpResponseRule>();
		for (List<HttpResponseRule> r : responseRuleMap.values()) {
			rules.addAll(r);
		}
		return rules;
	}

	@Override
	public Collection<HttpResponseRule> getResponseRules(String id) {
		return responseRuleMap.get(id);
	}

	@Override
	public Collection<HttpRequestRule> matchAll(HttpRequestContext context) {
		Set<HttpRequestRule> matches = new HashSet<HttpRequestRule>();
		byte[] bytes = getPathBytes(context.getPath());
		SearchContext sctx = new SearchContext();
		sctx.setIncludeFailurePatterns(true);
		List<Pair> pairs = acm.search(bytes, sctx);

		for (Pair p : pairs) {
			Rule rule = ((RulePattern) p.getPattern()).getRule();
			HttpRequestRule httpRule = (HttpRequestRule) rule;
			if (httpRule.match(context))
				matches.add(httpRule);
		}

		return matches;
	}

	@Override
	public HttpRequestRule match(HttpRequestContext context) {
		byte[] bytes = getPathBytes(context.getPath());
		SearchContext sctx = new SearchContext();
		sctx.setIncludeFailurePatterns(true);
		List<Pair> pairs = acm.search(bytes, sctx);

		for (Pair p : pairs) {
			Rule rule = ((RulePattern) p.getPattern()).getRule();
			HttpRequestRule httpRule = (HttpRequestRule) rule;
			if (httpRule.match(context))
				return httpRule;
		}

		return null;
	}

	private byte[] getPathBytes(String path) {
		byte[] bytes = null;
		try {
			bytes = path.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		return bytes;
	}

	@Override
	public HttpResponseRule match(HttpRequestContext req, HttpResponseContext resp) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public Collection<HttpResponseRule> matchAll(HttpRequestContext req, HttpResponseContext resp) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	private static class RuleFileFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".rules");
		}
	}

	private static class RulePattern implements Pattern {
		private byte[] b;
		private Rule rule;

		public RulePattern(String path, Rule rule) throws UnsupportedEncodingException {
			this.b = path.getBytes("utf-8");
			this.rule = rule;
		}

		public Rule getRule() {
			return rule;
		}

		@Override
		public byte[] getKeyword() {
			return b;
		}

		@Override
		public String toString() {
			return rule.toString();
		}
	}
}

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
package org.krakenapps.logstorage.engine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.logstorage.IndexConfigSpec;
import org.krakenapps.logstorage.IndexTokenizer;
import org.krakenapps.logstorage.IndexTokenizerFactory;
import org.krakenapps.logstorage.IndexTokenizerRegistry;

/**
 * 
 * @author xeraph
 * @since 0.9
 */
@Component(name = "logstorage-regex-index-tokenizer-factory")
public class RegexIndexTokenizerFactory implements IndexTokenizerFactory {
	@Requires
	private IndexTokenizerRegistry registry;

	@Override
	public String getName() {
		return "regex";
	}

	@Validate
	public void start() {
		registry.registerFactory(this);
	}

	@Invalidate
	public void stop() {
		if (registry != null)
			registry.unregisterFactory(this);
	}

	@Override
	public List<IndexConfigSpec> getConfigSpecs() {
		IndexConfigSpec regex = new IndexConfigSpecImpl("regex", true, "regex", "matcher regular expression");
		return Arrays.asList(regex);
	}

	@Override
	public IndexTokenizer newIndexTokenizer(Map<String, String> config) {
		String regex = (String) config.get("regex");
		if (regex == null)
			throw new IllegalArgumentException("regex is required");

		return new RegexIndexTokenizer(Pattern.compile(regex));
	}

	private static class RegexIndexTokenizer implements IndexTokenizer {
		private Pattern p;

		public RegexIndexTokenizer(Pattern p) {
			this.p = p;
		}

		@Override
		public Set<String> tokenize(Map<String, Object> m) {
			HashSet<String> tokens = new HashSet<String>();
			for (Object v : m.values()) {
				if (v == null)
					continue;

				if (v instanceof String) {
					String s = (String) v;
					Matcher matcher = p.matcher(s);
					while (matcher.find())
						tokens.add(matcher.group());
				}
			}

			return tokens;
		}
	}
}

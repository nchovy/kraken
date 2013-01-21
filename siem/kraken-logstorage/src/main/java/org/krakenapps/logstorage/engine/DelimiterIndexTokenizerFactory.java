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
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.logstorage.IndexConfigSpec;
import org.krakenapps.logstorage.IndexTokenizer;
import org.krakenapps.logstorage.IndexTokenizerFactory;
import org.krakenapps.logstorage.IndexTokenizerRegistry;

/**
 * @since 0.9
 * @author xeraph
 */
@Component(name = "logstorage-delimiter-index-tokenizer-factory")
public class DelimiterIndexTokenizerFactory implements IndexTokenizerFactory {

	@Requires
	private IndexTokenizerRegistry registry;

	private AtomicLong logCounter = new AtomicLong();
	private AtomicLong tokenCounter = new AtomicLong();

	@Override
	public String getName() {
		return "delimiter";
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
		IndexConfigSpec delims = new IndexConfigSpecImpl("delimiters", true, "delimiters", "delimiter characters");
		IndexConfigSpec targets = new IndexConfigSpecImpl("target_columns", false, "target columns",
				"index target columns which are separated by comma");
		return Arrays.asList(delims, targets);
	}

	@Override
	public IndexTokenizer newIndexTokenizer(Map<String, String> config) {
		return new DelimiterIndexTokenizer(config);
	}

	private class DelimiterIndexTokenizer implements IndexTokenizer {
		private String delimiters;
		private String[] targetColumns;

		public DelimiterIndexTokenizer(Map<String, String> config) {
			delimiters = config.get("delimiters");
			String s = config.get("target_columns");
			if (s != null) {
				targetColumns = s.split(",");
			}
		}

		@Override
		public Set<String> tokenize(Map<String, Object> m) {
			HashSet<String> set = new HashSet<String>();
			logCounter.incrementAndGet();
			if (targetColumns == null) {
				for (Object value : m.values()) {
					if (value != null) {
						if (value instanceof String) {
							String s = ((String) value).toLowerCase();
							StringTokenizer tok = new StringTokenizer(s, delimiters);
							while (tok.hasMoreTokens()) {
								set.add(tok.nextToken());
								tokenCounter.incrementAndGet();
							}
						} else if (value instanceof Number) {
							set.add(value.toString());
							tokenCounter.incrementAndGet();
						}
					}
				}
			} else {
				for (String target : targetColumns) {
					Object value = m.get(target);
					if (value != null) {
						if (value instanceof String) {
							String s = ((String) value).toLowerCase();
							StringTokenizer tok = new StringTokenizer(s, delimiters);
							while (tok.hasMoreTokens()) {
								set.add(tok.nextToken());
								tokenCounter.incrementAndGet();
							}
						} else if (value instanceof Number) {
							set.add(value.toString());
							tokenCounter.incrementAndGet();
						}
					}
				}
			}
			return set;
		}
	}

	@Override
	public String toString() {
		return "delimiter index tokenizer factory, processed tokens=" + tokenCounter.get() + ", logs=" + logCounter.get();
	}

}

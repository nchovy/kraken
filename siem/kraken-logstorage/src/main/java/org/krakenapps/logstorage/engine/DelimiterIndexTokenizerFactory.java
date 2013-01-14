package org.krakenapps.logstorage.engine;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.logstorage.IndexTokenizer;
import org.krakenapps.logstorage.IndexTokenizerFactory;
import org.krakenapps.logstorage.IndexTokenizerRegistry;

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
				targetColumns = s.split(" ");
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

/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.logdb.query.command;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.query.ObjectComparator;
import org.krakenapps.logdb.sort.CloseableIterator;
import org.krakenapps.logdb.sort.Item;
import org.krakenapps.logdb.sort.ParallelMergeSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stats2 extends LogQueryCommand {
	private final Logger logger = LoggerFactory.getLogger(Stats2.class);
	private int inputCount;
	private List<String> clauses;
	private Function[] values;
	private ParallelMergeSorter sorter;

	public Stats2(List<String> clause, Function[] values) {
		this.clauses = clause;
		this.values = values;
		this.sorter = new ParallelMergeSorter(new ItemComparer());
	}

	@Override
	public void init() {
		super.init();

		for (Function f : values)
			f.clean();
	}

	@Override
	public void push(LogMap m) {
		Object[] keys = new Object[clauses.size()];
		int i = 0;
		for (String clause : clauses) {
			Object keyValue = m.get(clause);
			if (keyValue == null)
				return;

			keys[i++] = keyValue;
		}

		try {
			inputCount++;
			sorter.add(new Item(keys, filt(m)));
		} catch (IOException e) {
			throw new IllegalStateException("sort failed, query " + logQuery, e);
		}
	}

	private Map<String, Object> filt(LogMap l) {
		Map<String, Object> m = new HashMap<String, Object>();
		for (Function f : values) {
			String k = f.getTarget();
			if (k != null)
				m.put(k, l.get(k));
		}
		return m;
	}

	@Override
	public boolean isReducer() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void eof() {
		logger.debug("kraken logdb: sort input count [{}]", inputCount);
		CloseableIterator it = null;
		try {
			it = sorter.sort();

			Object[] lastKeys = null;
			Function[] fs = null;
			Item item = null;
			int count = 0;
			while (it.hasNext()) {
				item = (Item) it.next();
				count++;

				if (lastKeys == null || !Arrays.equals(lastKeys, (Object[]) item.getKey())) {

					// finalize last record
					if (fs != null) {
						pass(fs, item);
					}

					// prepare new record
					fs = new Function[values.length];
					for (int i = 0; i < fs.length; i++)
						fs[i] = values[i].clone();
				}

				for (Function f : fs)
					f.put(new LogMap((Map<String, Object>) item.getValue()));

				lastKeys = (Object[]) item.getKey();
			}

			if (item != null)
				pass(fs, item);

			logger.debug("kraken logdb: sorted stats2 input [{}]", count);
		} catch (IOException e) {
			throw new IllegalStateException("sort failed, query " + logQuery, e);
		} finally {
			if (it != null) {
				try {
					// close and delete final sorted run file
					it.close();
				} catch (IOException e) {
				}
			}
			super.eof();
		}
	}

	private void pass(Function[] fs, Item item) {
		Map<String, Object> m = new HashMap<String, Object>();

		for (int i = 0; i < clauses.size(); i++)
			m.put(clauses.get(i), ((Object[]) item.getKey())[i]);

		for (int i = 0; i < values.length; i++)
			m.put(values[i].toString(), fs[i].getResult());

		write(new LogMap(m));
	}

	private static class ItemComparer implements Comparator<Item> {
		private ObjectComparator cmp = new ObjectComparator();

		@Override
		public int compare(Item o1, Item o2) {
			return cmp.compare(o1.getKey(), o2.getKey());
		}

	}
}

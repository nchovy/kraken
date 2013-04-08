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
import java.util.ArrayList;
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

	private Map<List<Object>, Function[]> buffer;

	public Stats2(List<String> clause, Function[] values) {
		this.clauses = clause;
		this.values = values;
		this.sorter = new ParallelMergeSorter(new ItemComparer());
		this.buffer = new HashMap<List<Object>, Function[]>();
	}

	@Override
	public void init() {
		super.init();

		for (Function f : values)
			f.clean();
	}

	@Override
	public void push(LogMap m) {
		List<Object> keys = new ArrayList<Object>(clauses.size());
		for (String clause : clauses) {
			Object keyValue = m.get(clause);
			if (keyValue == null)
				return;

			keys.add(keyValue);
		}

		try {
			inputCount++;

			Function[] fs = buffer.get(keys);
			if (fs == null) {
				fs = new Function[values.length];
				for (int i = 0; i < fs.length; i++)
					fs[i] = values[i].clone();

				buffer.put(keys, fs);
			}

			for (Function f : fs)
				f.put(m);

			// flush
			if (buffer.keySet().size() > 50000)
				flush();

		} catch (IOException e) {
			throw new IllegalStateException("sort failed, query " + logQuery, e);
		}
	}

	private void flush() throws IOException {
		logger.debug("kraken logdb: flushing stats2 buffer, [{}] keys", buffer.keySet().size());

		for (List<Object> keys : buffer.keySet()) {
			Function[] fs = buffer.get(keys);
			Object[] l = new Object[fs.length];
			int i = 0;
			for (Function f : fs)
				l[i++] = f.serialize();

			sorter.add(new Item(keys.toArray(), l));
		}

		buffer.clear();
	}

	@Override
	public boolean isReducer() {
		return true;
	}

	@Override
	public void eof() {
		this.status = Status.Finalizing;

		logger.debug("kraken logdb: stats2 sort input count [{}]", inputCount);
		CloseableIterator it = null;
		try {
			// last flush
			flush();

			// reclaim buffer (GC support)
			buffer = null;

			// sort
			it = sorter.sort();

			Object[] lastKeys = null;
			Function[] fs = null;
			Item item = null;
			int count = 0;
			while (it.hasNext()) {
				item = (Item) it.next();
				count++;

				// first record or need to change merge set?
				if (lastKeys == null || !Arrays.equals(lastKeys, (Object[]) item.getKey())) {
					if (logger.isDebugEnabled() && lastKeys != null)
						logger.debug("kraken logdb: stats2 key compare [{}] != [{}]", lastKeys[0], ((Object[]) item.getKey())[0]);

					// finalize last record (only if changing set)
					if (fs != null) {
						pass(fs, lastKeys);
					}

					// load new record
					fs = new Function[values.length];
					int i = 0;
					Object[] rawFuncs = (Object[]) item.getValue();
					for (Object rawFunc : rawFuncs) {
						Object[] l = (Object[]) rawFunc;
						String name = (String) l[0];
						String target = (String) l[1];
						String keyName = (String) l[2];
						Function f = Function.getFunction(name, target, keyName, Timechart.func);
						f.load(l);
						fs[i++] = f;
					}
				} else {
					// merge
					int i = 0;
					for (Function f : fs) {
						Object[] l = (Object[]) ((Object[]) item.getValue())[i];
						String name = (String) l[0];
						String target = (String) l[1];
						String keyName = (String) l[2];
						Function f2 = Function.getFunction(name, target, keyName, Timechart.func);
						f2.load(l);
						f.merge(f2);
						i++;
					}
				}

				lastKeys = (Object[]) item.getKey();
			}

			// write last merge set
			if (item != null)
				pass(fs, lastKeys);

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

			// support sorter cache GC when query processing is ended
			sorter = null;

			super.eof();
		}
	}

	private void pass(Function[] fs, Object[] keys) {
		Map<String, Object> m = new HashMap<String, Object>();

		for (int i = 0; i < clauses.size(); i++)
			m.put(clauses.get(i), keys[i]);

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

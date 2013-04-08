/*
 * Copyright 2011 Future Systems
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
import java.util.Comparator;
import java.util.Map;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.query.ObjectComparator;
import org.krakenapps.logdb.sort.CloseableIterator;
import org.krakenapps.logdb.sort.Item;
import org.krakenapps.logdb.sort.ParallelMergeSorter;

public class Sort2 extends LogQueryCommand {
	private Integer limit;
	private SortField[] fields;
	private ParallelMergeSorter sorter;
	private boolean reverse;

	public Sort2(SortField[] fields) throws IOException {
		this(null, fields, false);
	}

	public Sort2(Integer limit, SortField[] fields) throws IOException {
		this(limit, fields, false);
	}

	public Sort2(SortField[] fields, boolean reverse) throws IOException {
		this(null, fields, reverse);
	}

	public Sort2(Integer limit, SortField[] fields, boolean reverse) throws IOException {
		this.limit = limit;
		this.fields = fields;
		this.reverse = reverse;
	}

	@Override
	public void init() {
		super.init();
		this.sorter = new ParallelMergeSorter(new DefaultComparator());
	}

	public Integer getLimit() {
		return limit;
	}

	public SortField[] getFields() {
		return fields;
	}

	public boolean isReverse() {
		return reverse;
	}

	@Override
	public void push(LogMap m) {
		try {
			sorter.add(new Item(m.map(), null));
		} catch (IOException e) {
			throw new IllegalStateException("sort failed, query " + logQuery, e);
		}
	}

	@Override
	public boolean isReducer() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void eof() {
		this.status = Status.Finalizing;
		// TODO: use LONG instead!
		int count = limit != null ? limit : Integer.MAX_VALUE;

		CloseableIterator it = null;
		try {
			it = sorter.sort();

			while (it.hasNext()) {
				Object o = it.next();
				if (--count < 0)
					break;

				Map<String, Object> value = (Map<String, Object>) ((Item) o).getKey();
				write(new LogMap(value));
			}

		} catch (IOException e) {
		} finally {
			// close and delete sorted run file
			if (it != null) {
				try {
					it.close();
				} catch (IOException e) {
				}
			}
		}

		// support sorter cache GC when query processing is ended
		sorter = null;
		
		super.eof();
	}

	private class DefaultComparator implements Comparator<Item> {
		private ObjectComparator cmp = new ObjectComparator();

		@SuppressWarnings("unchecked")
		@Override
		public int compare(Item o1, Item o2) {
			Map<String, Object> m1 = (Map<String, Object>) o1.getKey();
			Map<String, Object> m2 = (Map<String, Object>) o2.getKey();

			for (SortField field : fields) {
				Object v1 = m1.get(field.name);
				Object v2 = m2.get(field.name);

				if (v1 == null && v2 == null)
					continue;
				else if (v1 == null && v2 != null)
					return 1;

				if (!v1.equals(v2)) {
					int result = cmp.compare(v1, v2);

					if (!field.asc)
						result *= -1;
					if (reverse)
						result *= -1;

					return result;
				}
			}

			return 0;
		}
	}

	public static class SortField {
		private String name;
		private boolean asc;

		public SortField(String name) {
			this(name, true);
		}

		public SortField(String name, boolean asc) {
			this.name = name;
			this.asc = asc;
		}

		public String getName() {
			return name;
		}

		public boolean isAsc() {
			return asc;
		}

		public void reverseAsc() {
			asc = !asc;
		}
	}
}

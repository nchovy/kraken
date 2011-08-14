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
package org.krakenapps.logstorage.query.command;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;

import org.krakenapps.logstorage.LogQueryCommand;
import org.krakenapps.logstorage.query.ObjectComparator;
import org.krakenapps.logstorage.query.FileBufferList;

public class Sort extends LogQueryCommand {
	private Integer count;
	private SortField[] fields;
	private FileBufferList<Map<String, Object>> buf;
	private boolean reverse;

	public Sort(SortField[] fields) throws IOException {
		this(null, fields, false);
	}

	public Sort(Integer count, SortField[] fields) throws IOException {
		this(count, fields, false);
	}

	public Sort(SortField[] fields, boolean reverse) throws IOException {
		this(null, fields, reverse);
	}

	public Sort(Integer count, SortField[] fields, boolean reverse) throws IOException {
		this.count = count;
		this.fields = fields;
		this.reverse = reverse;
	}

	public Integer getCount() {
		return count;
	}

	public SortField[] getFields() {
		return fields;
	}

	public boolean isReverse() {
		return reverse;
	}

	@Override
	public void push(Map<String, Object> m) {
		if (buf == null) {
			try {
				this.buf = new FileBufferList<Map<String, Object>>(new DefaultComparator());
			} catch (IOException e) {
			}
		}
		buf.add(m);
	}

	@Override
	public void eof() {
		if (count == null) {
			write(buf);
		} else {
			if (buf != null) {
				for (Map<String, Object> m : buf) {
					if (--count < 0)
						break;
					write(m);
				}
				buf.close();
			}
			buf = null;
		}

		super.eof();
	}

	private class DefaultComparator implements Comparator<Map<String, Object>> {
		private ObjectComparator cmp = new ObjectComparator();

		@Override
		public int compare(Map<String, Object> m1, Map<String, Object> m2) {
			for (SortField field : fields) {
				Object o1 = getData(field.name, m1);
				Object o2 = getData(field.name, m2);

				if (o1 == null && o2 == null)
					continue;
				else if (o1 == null && o2 != null)
					return 1;

				if (!o1.equals(o2)) {
					int result = cmp.compare(o1, o2);

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

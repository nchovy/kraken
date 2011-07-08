package org.krakenapps.logstorage.query.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.krakenapps.logstorage.query.LogQueryCommand;

public class Sort extends LogQueryCommand {
	private Integer count;
	private SortField[] fields;
	private List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
	private boolean reverse;

	public Sort(SortField[] fields) {
		this(null, fields, false);
	}

	public Sort(Integer count, SortField[] fields) {
		this(count, fields, false);
	}

	public Sort(SortField[] fields, boolean reverse) {
		this(null, fields, reverse);
	}

	public Sort(Integer count, SortField[] fields, boolean reverse) {
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
		result.add(m);
	}

	@Override
	public void eof() {
		Collections.sort(result, new DefaultComparator());

		if (count == null)
			count = result.size();

		for (Map<String, Object> m : result) {
			if (count-- <= 0)
				break;
			write(m);
		}

		result = null;
		super.eof();
	}

	private class DefaultComparator implements Comparator<Map<String, Object>> {
		@Override
		public int compare(Map<String, Object> m1, Map<String, Object> m2) {
			for (SortField field : fields) {
				Object o1 = m1.get(field.name);
				Object o2 = m2.get(field.name);

				if (o1 == null && o2 == null)
					continue;
				else if (o1 == null && o2 != null)
					return 1;

				if (!o1.equals(o2)) {
					int result = 0;

					if (o1 instanceof String && o2 instanceof String)
						result = ((String) o1).compareTo((String) o2);
					else if (o1 instanceof Integer && o2 instanceof Integer)
						result = (((Integer) o1) - ((Integer) o2));

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

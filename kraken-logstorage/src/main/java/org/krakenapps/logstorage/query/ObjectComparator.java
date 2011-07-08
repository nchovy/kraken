package org.krakenapps.logstorage.query;

import java.util.Comparator;

public class ObjectComparator implements Comparator<Object> {
	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == null && o2 == null)
			return 0;

		else if (o1 == null && o2 != null)
			return 1;

		if (!o1.equals(o2)) {
			int result = 0;

			if (o1 instanceof String && o2 instanceof String)
				result = ((String) o1).compareTo((String) o2);
			else if (o1 instanceof Integer && o2 instanceof Integer)
				result = (((Integer) o1) - ((Integer) o2));

			return result;
		}

		return 0;
	}
}

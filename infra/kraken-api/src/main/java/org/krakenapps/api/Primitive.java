package org.krakenapps.api;

import java.util.Collection;
import java.util.Map;

public class Primitive {
	private Primitive() {
	}

	public static String stringify(Object o) {
		StringBuilder sb = new StringBuilder();
		stringify(sb, o);
		return sb.toString();
	}

	private static void stringify(StringBuilder sb, Object o) {
		if (o instanceof Object[]) {
			Object[] arr = (Object[]) o;
			sb.append("[");
			int i = 0;
			for (Object child : arr) {
				if (i++ != 0)
					sb.append(", ");
				stringify(sb, child);
			}
			sb.append("]");
		} else if (o instanceof Collection<?>) {
			sb.append("[");
			int i = 0;
			for (Object child : (Collection<?>) o) {
				if (i++ != 0)
					sb.append(", ");
				stringify(sb, child);
			}
			sb.append("]");
		} else if (o instanceof Map<?, ?>) {
			Map<?, ?> m = (Map<?, ?>) o;
			sb.append("{");
			int i = 0;
			for (Object k : m.keySet()) {
				if (i++ != 0)
					sb.append(", ");

				sb.append("\"");
				sb.append(k.toString());
				sb.append("\": ");
				stringify(sb, m.get(k));
			}

			sb.append("}");
		} else {
			if (o == null)
				sb.append("null");
			else {
				sb.append("\"");
				sb.append(o.toString());
				sb.append("\"");
			}
		}
	}
}

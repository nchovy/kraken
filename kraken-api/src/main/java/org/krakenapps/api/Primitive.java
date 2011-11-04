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
		if (o instanceof Collection<?>) {
			sb.append("[");
			for (Object child : (Collection<?>) o)
				stringify(sb, child);
			sb.append("]");
		} else if (o instanceof Map<?, ?>) {
			Map<?, ?> m = (Map<?, ?>) o;
			sb.append("{");
			for (Object k : m.keySet()) {
				sb.append("\"");
				sb.append(k.toString());
				sb.append("\": ");
				stringify(sb, m.get(k));
			}

			sb.append("}");
		} else {
			sb.append("\"");
			sb.append(o.toString());
			sb.append("\"");
		}
	}
}

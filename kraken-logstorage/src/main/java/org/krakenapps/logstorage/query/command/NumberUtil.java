package org.krakenapps.logstorage.query.command;

public class NumberUtil {
	public static Class<? extends Number> getClass(Object obj) {
		if (obj == null)
			return null;
		try {
			Long.parseLong(obj.toString());
			return Long.class;
		} catch (NumberFormatException e) {
		}
		try {
			Double.parseDouble(obj.toString());
			return Double.class;
		} catch (NumberFormatException e) {
		}
		return null;
	}

	public static Number getValue(Object obj) {
		return getValue(obj, null);
	}

	public static Number getValue(Object obj, Number defaultValue) {
		if (obj == null)
			return defaultValue;
		try {
			return Long.parseLong(obj.toString());
		} catch (NumberFormatException e) {
		}
		try {
			return Double.parseDouble(obj.toString());
		} catch (NumberFormatException e) {
		}
		return defaultValue;
	}

	public static Number add(Object o1, Object o2) {
		Number n1 = getValue(o1, 0L);
		Number n2 = getValue(o2, 0L);
		if (n1 instanceof Long && n2 instanceof Long)
			return n1.longValue() + n2.longValue();
		else
			return n1.doubleValue() + n2.doubleValue();
	}

	public static Number sub(Object o1, Object o2) {
		Number n1 = getValue(o1, 0L);
		Number n2 = getValue(o2, 0L);
		if (n1 instanceof Long && n2 instanceof Long)
			return n1.longValue() - n2.longValue();
		else
			return n1.doubleValue() - n2.doubleValue();
	}

	public static Number mul(Object o1, Object o2) {
		Number n1 = getValue(o1, 0L);
		Number n2 = getValue(o2, 0L);
		if (n1 instanceof Long && n2 instanceof Long)
			return n1.longValue() * n2.longValue();
		else
			return n1.doubleValue() * n2.doubleValue();
	}

	public static Number div(Object o1, Object o2) {
		Number n1 = getValue(o1, 0L);
		Number n2 = getValue(o2, 0L);
		return n1.doubleValue() / n2.doubleValue();
	}

	public static Number mod(Object o1, Object o2) {
		Number n1 = getValue(o1, 0L);
		Number n2 = getValue(o2, 0L);
		if (n1 instanceof Long && n2 instanceof Long)
			return n1.longValue() % n2.longValue();
		else
			return n1.doubleValue() % n2.doubleValue();
	}

	public static Number max(Object o1, Object o2) {
		Number n1 = getValue(o1);
		Number n2 = getValue(o2);

		if (n1 == null)
			return n2;
		if (n2 == null)
			return n1;

		if (n1.doubleValue() > n2.doubleValue())
			return n1;
		else
			return n2;
	}

	public static Number min(Object o1, Object o2) {
		Number n1 = getValue(o1);
		Number n2 = getValue(o2);

		if (n1 == null)
			return n2;
		if (n2 == null)
			return n1;

		if (n1.doubleValue() < n2.doubleValue())
			return n1;
		else
			return n2;
	}
}

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
		if (obj.toString().startsWith("0x")) {
			try {
				Long.parseLong(obj.toString().substring(2), 16);
				return Long.class;
			} catch (NumberFormatException e) {
			}
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
		if (obj.toString().startsWith("0x")) {
			try {
				return Long.parseLong(obj.toString().substring(2), 16);
			} catch (NumberFormatException e) {
			}
		}
		return defaultValue;
	}

	public static boolean eq(Object o1, Object o2) {
		Number n1 = getValue(o1);
		Number n2 = getValue(o2);
		if (n1 == null || n2 == null)
			return false;
		return (n1.doubleValue() == n2.doubleValue());
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

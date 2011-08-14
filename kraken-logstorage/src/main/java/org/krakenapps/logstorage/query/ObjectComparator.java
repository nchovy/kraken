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
package org.krakenapps.logstorage.query;

import java.util.Comparator;
import java.util.Date;

public class ObjectComparator implements Comparator<Object> {
	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == null && o2 == null)
			return 0;
		else if (o1 == null && o2 != null)
			return 1;
		else if (o1 != null && o2 == null)
			return -1;

		if (!o1.equals(o2)) {
			if (!o1.getClass().equals(o2.getClass()))
				return o1.toString().compareTo(o2.toString());

			if (o1 instanceof String)
				return ((String) o1).compareTo((String) o2);
			else if (o1 instanceof Short)
				return ((Short) o1).compareTo((Short) o2);
			else if (o1 instanceof Integer)
				return ((Integer) o1).compareTo((Integer) o2);
			else if (o1 instanceof Long)
				return ((Long) o1).compareTo((Long) o2);
			else if (o1 instanceof Double)
				return ((Double) o1).compareTo((Double) o2);
			else if (o1 instanceof Date)
				return ((Date) o1).compareTo((Date) o2);
		}

		return 0;
	}
}

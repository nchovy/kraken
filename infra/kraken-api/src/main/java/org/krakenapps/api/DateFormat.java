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
package org.krakenapps.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormat {
	private SimpleDateFormat sdf;

	public DateFormat(String pattern) {
		this.sdf = new SimpleDateFormat(pattern);
	}

	public String format(Date date) {
		if (date == null)
			return null;

		return sdf.format(date);
	}

	public Date parse(String source) {
		if (source == null)
			return null;

		try {
			return sdf.parse(source);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static String format(String pattern, Date date) {
		return new DateFormat(pattern).format(date);
	}

	public static Date parse(String pattern, String source) {
		return new DateFormat(pattern).parse(source);
	}
}

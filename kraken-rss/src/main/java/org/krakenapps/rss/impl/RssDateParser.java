/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.rss.impl;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public class RssDateParser {
	public static Date parse(String date) {
		try {
			String datePattern = null;
			String datePattern1 = "[A-Za-z]{3}, \\s*\\d{1,2} [A-Za-z]{3} \\d{4} \\d{2}:\\d{2}:\\d{2} [+-]{1}\\d{4}";
			String datePattern2 = "[A-Za-z]{3}, \\s*\\d{1,2} [A-Za-z]{3} \\d{4} \\d{2}:\\d{2}:\\d{2} GMT";
			String datePattern3 = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}";
			String datePattern4 = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}-\\d{2}:\\d{2}";
			String datePattern5 = "\\d{4}-\\d{2}-\\d{2}";
			String datePattern6 = "\\d{4}-\\d{2}-\\d{2} ";
			String datePattern7 = "\\d{1,2} [A-Za-z]{3,4} \\d{4} \\d{2}:\\d{2}:\\d{2} Z";
			String datePattern8 = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}[+-]{1}\\d{2}:\\d{2}";
			String datePattern9 = "";
			String normalDatePattern = "yyyy-MM-dd HH:mm:ss";

			if (Pattern.matches(datePattern1, date)
					| Pattern.matches(datePattern2, date))
				datePattern = "EEE, dd MMM yyyy HH:mm:ss Z";
			else if (Pattern.matches(datePattern3, date))
				datePattern = "yyyy-MM-dd'T'HH:mm:ss";
			else if (Pattern.matches(datePattern4, date)) {
				String timeZone = date.substring(19);
				String newTimeZone = timeZone.replace(":", "");
				date = date.substring(0, 19) + newTimeZone;
				datePattern = "yyyy-MM-dd'T'HH:mm:ssZ";
			} else if (Pattern.matches(datePattern5, date)
					|| Pattern.matches(datePattern6, date)) {
				Calendar cal = Calendar.getInstance();
				String time = String.format("%02d:%02d:%02d", cal
						.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
						cal.get(Calendar.SECOND));
				date = date.replaceAll(" ", "") + "T" + time;
				datePattern = "yyyy-MM-dd'T'HH:mm:ss";
			} else if (Pattern.matches(datePattern7, date))
				datePattern = "dd MMM yyyy HH:mm:ss 'Z'";
			else if (Pattern.matches(datePattern8, date)) {
				date = date.substring(0, 26) + "00";
				datePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
			} else if (Pattern.matches(datePattern9, date))
				return null;

			java.text.SimpleDateFormat dateFormat = new SimpleDateFormat(
					datePattern, Locale.US);
			java.text.SimpleDateFormat normalDateFormat = new SimpleDateFormat(
					normalDatePattern);
			Date newDate = dateFormat.parse(date);
			String inputDate = normalDateFormat.format(newDate);
			Date resultDate = normalDateFormat.parse(inputDate);

			return resultDate;
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
	}
}
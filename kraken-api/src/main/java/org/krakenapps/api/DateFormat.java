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

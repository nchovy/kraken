package org.krakenapps.util;

import java.util.Date;

public class DateUtil {
	public static Date normalizeDate(Date target, int resolution) {
		return new Date(normalize(target.getTime(), resolution * 1000));
	}

	public static Date normalizeDate(Date target, int resolution, int bias) {
		return new Date(normalize(target.getTime(), resolution * 1000) + bias * 1000);
	}

	private static long normalize(long lBegin, int resolution) {
		return (lBegin / resolution + 0/*(lBegin % resolution == 0 ? 0 : 1)*/) * resolution;
	}
}

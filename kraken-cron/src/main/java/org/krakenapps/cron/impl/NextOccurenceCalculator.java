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
package org.krakenapps.cron.impl;

import java.util.Calendar;
import java.util.Date;

import org.krakenapps.cron.Schedule;

public abstract class NextOccurenceCalculator {
	private static final int MONTH_TYPE = 1;
	private static final int DAY_TYPE = 2;
	private static final int HOUR_TYPE = 3;
	private static final int MINUTE_TYPE = 4;
	private static final int None = -1;

	private static void setAllFirstValue(Schedule sche, Calendar base, NextOccurence next, int type) throws Exception {
		for (int i = type; i < 5; i++) {
			next.set(i, getFirstValue(sche, i, base));
		}
	}

	/**
	 * returns the first date corresponding to the schedule, given current time
	 */
	public static Date getNextOccurence(Schedule sche, Date current) {

		Calendar base = Calendar.getInstance();
		NextOccurence next = new NextOccurence();

		base.setTime(current);
		base.set(Calendar.SECOND, 0);

		for (int type = MINUTE_TYPE; type > 0; type--)
			adjustNextOccurence(sche, base, next, type);

		next.set(base.get(Calendar.YEAR), None, None, None, None);

		return newDate(next);
	}

	private static final int calendarTypes[] = { Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,
			Calendar.HOUR_OF_DAY, Calendar.MINUTE };

	private static final int upperCalendarTypes[] = { 0, Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,
			Calendar.HOUR_OF_DAY };

	private static void adjustNextOccurence(Schedule sche, Calendar base, NextOccurence next, int type) {
		try {
			int test = getNextValue(sche, type, base);
			if (test == None) {
				base.add(upperCalendarTypes[type], 1);
				if (type == DAY_TYPE && getFirstValue(sche, DAY_TYPE, base) == None)
					base.add(upperCalendarTypes[type], 1);

				if (type == MONTH_TYPE)
					base.set(Calendar.MONTH, getFirstValue(sche, type, base));

				setAllFirstValue(sche, base, next, type);
			} else if (test > base.get(calendarTypes[type])) {
				if (type == MONTH_TYPE)
					base.set(Calendar.MONTH, test);

				next.set(type, test);
				setAllFirstValue(sche, base, next, type + 1);
			} else {
				next.set(type, test);
			}
		} catch (Exception e) {
			// must succeed. ignore.
		}
	}

	private static int getFirstValue(Schedule sche, int type, Calendar base) throws Exception {
		switch (type) {
		case MONTH_TYPE:
			return sche.get(CronField.Type.MONTH).first();
		case DAY_TYPE:
			return sche.get(CronField.Type.DAY_OF_MONTH).first(base, sche.get(CronField.Type.DAY_OF_WEEK));
		case HOUR_TYPE:
			return sche.get(CronField.Type.HOUR).first();
		case MINUTE_TYPE:
			return sche.get(CronField.Type.MINUTE).first();
		}
		throw new Exception(" unsupported first value type");
	}

	private static int getNextValue(Schedule sche, int type, Calendar base) throws Exception {

		switch (type) {
		case MONTH_TYPE:
			return sche.get(CronField.Type.MONTH).next(base.get(Calendar.MONTH));
		case DAY_TYPE:
			return sche.get(CronField.Type.DAY_OF_MONTH).next(base, sche.get(CronField.Type.DAY_OF_WEEK)); // merge-or
		case HOUR_TYPE:
			return sche.get(CronField.Type.HOUR).next(base.get(Calendar.HOUR_OF_DAY));
		case MINUTE_TYPE:
			return sche.get(CronField.Type.MINUTE).next(base.get(Calendar.MINUTE));
		}
		throw new Exception(" unsupported first value type");

	}

	private static Date newDate(NextOccurence next) {
		Calendar result = Calendar.getInstance();
		result.set(Calendar.SECOND, 0);
		result.set(Calendar.MINUTE, next.minute);
		result.set(Calendar.HOUR_OF_DAY, next.hour);
		result.set(Calendar.DAY_OF_MONTH, next.day + 1); // offset
		result.set(Calendar.MONTH, next.month);
		result.set(Calendar.YEAR, next.year);
		return result.getTime();
	}

	public static class NextOccurence {
		private static final int None = -1;

		public int year;
		public int month;
		public int day;
		public int hour;
		public int minute;

		public void set(int type, int value) {
			switch (type) {
			case 1:
				this.month = value;
				break;
			case 2:
				this.day = value;
				break;
			case 3:
				this.hour = value;
				break;
			case 4:
				this.minute = value;
				break;
			}
		}

		public void set(int year, int month, int day, int hour, int minute) {
			if (year != None)
				this.year = year;

			if (month != None)
				this.month = month;

			if (day != None)
				this.day = day;

			if (hour != None)
				this.hour = hour;

			if (minute != None)
				this.minute = minute;
		}

		@Override
		public String toString() {
			return String.format("%d-%d-%d %d:%d", year, month, day, hour, minute);
		}

	}

}
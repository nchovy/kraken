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

import java.text.ParseException;
import java.util.BitSet;
import java.util.Calendar;

/**
 * component of Schedule. represents a field of the schedule.
 * 
 * @author periphery
 * @since 1.0.0
 */
public final class CronField {
	private final Type type;
	// bitmap index 0 represents 1 for CronField.type Month and dayOfMonth.
	private final BitSet bits;
	private final String exp;

	public enum Type {
		MINUTE("Minute", 60, 0), 
		HOUR("Hour", 24, 0), 
		DAY_OF_MONTH("DayOfMonth", 31, 1), 
		MONTH("Month", 12, 1), 
		DAY_OF_WEEK("DayOfWeek", 7, 0);

		private final String fieldName; // name of the type.
		private final int bitLength;
		private final int base;

		private Type(String fieldName, int bitLength, int base) {
			this.fieldName = fieldName;
			this.bitLength = bitLength;
			this.base = base;
		}

		public String toString() {
			return this.fieldName;
		}

		private int getBitLength() {
			return this.bitLength;
		}

		private int getBase() {
			return this.base;
		}

		private int getLast() {
			return this.getBase() + this.getBitLength() - 1;
		}

		/**
		 * return true if the num is out of range of given type. valid range of
		 * num: day of week (0 - 6) (Sunday=0) month (1 - 12) day of month (1 -
		 * 31) hour (0 - 23) min (0 - 59)
		 */
		private boolean invalid(int num) {
			return num < this.getBase() || num > this.getLast();
		}

		private void setBits(BitSet bits, int num) {
			if (this.base == 0)
				bits.set(num);
			else
				bits.set(num - 1);
		}

		/**
		 * initialize to all_false if type is day of week.
		 */
		private void setBits(BitSet bits) {
			if (this != DAY_OF_WEEK)
				bits.set(0, this.bitLength);
		}

		private boolean isAllTrue(BitSet bits) {
			return bits.nextClearBit(0) >= this.bitLength;
		}

		private boolean isAllFalse(BitSet bits) {
			return (bits.nextSetBit(0) == -1);
		}
	}

	public CronField(Type type, String exp) throws ParseException {
		this.type = type;
		this.bits = new BitSet(this.type.bitLength);
		this.exp = parseExp(this.type, this.bits, exp);
	}

	/**
	 * parse expression and set bitmap. if the expression is null, it is same as
	 * expression "*"(wild card)
	 */
	private static String parseExp(CronField.Type type, BitSet bits, String exp) throws ParseException {
		// filter "*" or null.
		if (exp == null || exp.matches("[*]")) {
			type.setBits(bits);
			return "*";
			// filter empty string. throw Exception.
		} else if (exp.length() == 0) {
			throw new ParseException("cron field cannot be set with an empty string : " + exp, 0);
			// filter list type.
		} else if (exp.matches("([0-9]+[,])+[0-9]+")) {
			String[] splited = exp.split("[,]");
			for (String sp : splited) {
				parseExp(type, bits, sp);// recursive call
			}
			// filter interval type
		} else if (exp.matches("[*]/[0-9]+")) {
			try {
				int interval = Integer.parseInt(exp.split("[/]")[1]);
				for (int i = type.getBase(); !type.invalid(i); i += interval) {
					type.setBits(bits, i);
				}
			} catch (Exception e) {
				throw new ParseException("wrong interval format. e.g. 2/* = every 2nd : " + exp, 0);
			}
			// filter range type
		} else if (exp.matches("[0-9]+[-][0-9]+")) {
			int from = Integer.parseInt(exp.split("[-]")[0]);
			int to = Integer.parseInt(exp.split("[-]")[1]);
			if (to < from || type.invalid(to) || type.invalid(from))
				throw new ParseException("invalida range. e.g. 2-5 = 2 to 5 : " + exp, 0);
			for (int i = from; !type.invalid(i) && i <= to; i++) {
				type.setBits(bits, i);
			}
			// filter single number
		} else if (exp.matches("[0-9]+")) {
			int num = Integer.parseInt(exp);
			if (type.invalid(num))
				throw new ParseException("value out of range : " + exp, 0);
			type.setBits(bits, num);
		} else
			throw new ParseException("wrong cron field format : " + exp, 0);
		return exp;
	}

	/**
	 * if day_of_week is not all_false and day_of_month is all_true, set
	 * day_of_month to all_false. this is because it is natural to think
	 * "0 0 * * 0" as 'weekly' not 'daily'. this is the reason why day_of_week
	 * is initially set to all_false.
	 * 
	 * @param dom
	 *            day of month
	 * @param dow
	 *            day of week
	 * @throws IllegalTypeException
	 *             when called with cronfield except for dom and dow.
	 */
	public static void solveCollision(CronField dom, CronField dow) throws IllegalTypeException {
		if (dom.type != Type.DAY_OF_MONTH || dow.type != Type.DAY_OF_WEEK)
			throw new IllegalTypeException("solveCollision should only be called with dom and dow.");

		if (dom.type.isAllTrue(dom.bits) && !dow.type.isAllFalse(dow.bits)) {
			dom.bits.clear();
		}
	}

	/**
	 * returns next matching occurrence after given start value.(including start
	 * value itself)
	 * 
	 * @param start
	 * @return next matching occurrence
	 * @throws IllegalTypeException
	 *             when called with dom or dow. should call next(Calender,
	 *             CronField) instead.
	 */
	public int next(int start) throws IllegalTypeException {
		if (this.type.equals(CronField.Type.DAY_OF_MONTH) || this.type.equals(CronField.Type.DAY_OF_WEEK))
			throw new IllegalTypeException(
					"not allowed for day_of_month and day_of_week. use next(Calendar, CronField) instead.");
		return this.bits.nextSetBit(start);
	}

	/**
	 * returns the first matching occurrence.
	 * 
	 * @return first matching occurrence.
	 * @throws IllegalTypeException
	 *             when called with dom or dow. should call first(Calender,
	 *             CronField) instead.
	 */
	public int first() throws IllegalTypeException {
		if (this.type.equals(CronField.Type.DAY_OF_MONTH) || this.type.equals(CronField.Type.DAY_OF_WEEK))
			throw new IllegalTypeException(
					"not allowed for day_of_month and day_of_week. use next(Calendar, CronField) instead.");
		
		return this.next(0);
	}

	/**
	 * returns next matching occurrence after given start value.(including start
	 * value itself)
	 * 
	 * @param base
	 * @param dow
	 *            day of week
	 * @return
	 * @throws IllegalTypeException
	 *             when called with a cronField except for dom
	 */
	public int next(Calendar base, CronField dow) throws IllegalTypeException {
		if (!this.type.equals(CronField.Type.DAY_OF_MONTH))
			throw new IllegalTypeException("only for day_of_month. use next(Calendar, CronField) instead.");
		
		return dow2month(base, dow).nextSetBit(base.get(Calendar.DAY_OF_MONTH) - 1);
	}

	/**
	 * returns the first matching occurrence.
	 * 
	 * @return first matching occurrence.
	 * @throws IllegalTypeException
	 *             when called with a cronField except for dom
	 */
	public int first(Calendar base, CronField dow) throws IllegalTypeException {
		if (!this.type.equals(CronField.Type.DAY_OF_MONTH))
			throw new IllegalTypeException("only for day_of_month. use next(Calendar, CronField) instead.");
		
		return dow2month(base, dow).nextSetBit(0);
	}

	// for merging day_of_month and day_of_week.
	// generates new bitmap.
	private BitSet dow2month(Calendar base, CronField dow) {
		Calendar clone = (Calendar) base.clone();
		clone.set(Calendar.DAY_OF_MONTH, 1);
		int weekDayOf_1 = clone.get(Calendar.DAY_OF_WEEK) - 1; // 1 for offset
		
		// merge dow and dom bitmaps.
		BitSet dow2month = mergeBits(weekDayOf_1, this.bits, dow.bits);
		// turn off for surplus days of month. (e.g. turn off 30 and 31 from
		// Feb.)
		
		clone.add(Calendar.MONTH, 1);
		clone.add(Calendar.DAY_OF_MONTH, -1);
		int lastDayOfMonth = clone.get(Calendar.DAY_OF_MONTH);
		
		for (int i = lastDayOfMonth; i < this.type.bitLength; i++)
			dow2month.clear(i);
		
		return dow2month;
	}

	// generate 31 length bitmap by duplicating dow bitmap.
	private static BitSet mergeBits(int weekDayOf_1, BitSet dom, BitSet dow) {
		BitSet dow2month = new BitSet();
		for (int i = 0; i < CronField.Type.DAY_OF_MONTH.getBitLength(); i++) {
			int weekDayOf_i = (i + weekDayOf_1) % 7;
			dow2month.set(i, dow.get(weekDayOf_i));
		}
		
		dow2month.or(dom); // dow bits and dom bits are merged by OR operation.
		return dow2month;
	}

	@Override
	public String toString() {
		return this.exp;
	}

	public String debugString() {
		return this.bits.toString();
	}
}

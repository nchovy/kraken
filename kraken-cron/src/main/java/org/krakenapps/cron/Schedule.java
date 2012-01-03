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
package org.krakenapps.cron;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.cron.impl.CronField;
import org.krakenapps.cron.impl.CronField.Type;

/**
 * Schedule used for registering cron jobs.
 * 
 * @author periphery
 * @since 1.0.0
 */
public final class Schedule {
	/**
	 * fieldName to CronField mapping
	 */
	private final Map<String, CronField> map;
	private final String taskName;

	private Schedule() {
		this.map = null;
		this.taskName = null;
	}

	private Schedule(Builder builder) {
		this.map = new HashMap<String, CronField>();
		this.map.put(Type.MINUTE.toString(), builder.map.get(Type.MINUTE.toString()));
		this.map.put(Type.HOUR.toString(), builder.map.get(Type.HOUR.toString()));
		this.map.put(Type.MONTH.toString(), builder.map.get(Type.MONTH.toString()));

		CronField dom = builder.map.get(Type.DAY_OF_MONTH.toString());
		CronField dow = builder.map.get(Type.DAY_OF_WEEK.toString());
		try {
			CronField.solveCollision(dom, dow);
		} catch (Exception e) {
			// must succeed. ignore.
		}
		this.map.put(Type.DAY_OF_MONTH.toString(), dom);
		this.map.put(Type.DAY_OF_WEEK.toString(), dow);
		this.taskName = builder.taskName;
	}

	/**
	 * returns task Name
	 */
	public String getTaskName() {
		return this.taskName;
	}

	@Override
	public String toString() {
		return String.format("%8s %8s %8s %8s %8s / %8s", map.get("Minute"), map.get("Hour"), map.get("DayOfMonth"), map.get("Month"),
				map.get("DayOfWeek"), taskName);
	}

	/**
	 * returns bitmap string of the given cron field used for debugging.
	 */
	public String fieldMembers(CronField.Type type) {
		return map.get(type.toString()).debugString();
	}

	/**
	 * returns members of the given cron field
	 */
	public CronField get(Type type) {
		return map.get(type.toString());
	}

	@Override
	public boolean equals(Object sche) {
		return (sche instanceof Schedule) && this.toString().equals(((Schedule) sche).toString());
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	/**
	 * builder class for schedule class
	 */
	public static class Builder {
		private final Map<String, CronField> map;
		private final String taskName;

		/**
		 * create new builder object. default scheduling rule is
		 * "* * * * *"(minutely).
		 */
		public Builder(String taskName) {
			this.map = new HashMap<String, CronField>();
			this.taskName = taskName;
			try {
				this.map.put("Minute", new CronField(CronField.Type.MINUTE, null));
				this.map.put("Hour", new CronField(CronField.Type.HOUR, null));
				this.map.put("DayOfMonth", new CronField(CronField.Type.DAY_OF_MONTH, null));
				this.map.put("Month", new CronField(CronField.Type.MONTH, null));
				this.map.put("DayOfWeek", new CronField(CronField.Type.DAY_OF_WEEK, null));
			} catch (ParseException e) {
				// must succeed. ignored.
			}
		}

		/**
		 * Set cron field with given expression. Following expressions are
		 * supported. 1. comma(',') as list. e.g:"1,3,4,8" (space inside the
		 * list must not be used) 2. dash('-') as range. e.g:"1-6", which means
		 * 1 to 6 3. asterisk('*') as wild. e.g:"*", which means every~ 4.
		 * slash('/') as interval. e.g:"* /5" which means every five~ (without
		 * whitespace)
		 */
		public Builder set(CronField.Type type, String exp) throws ParseException {
			this.map.put(type.toString(), new CronField(type, exp));
			return this;
		}

		/**
		 * returns schedule object representing scheduling rule of current build
		 * object. e.g. new
		 * Schedule.Builder("test").set(CronField.Type.Minute,"5").build();
		 * represents schedule of "5 * * * * / test"
		 */
		public Schedule build() {
			return new Schedule(this);
		}

		/**
		 * returns schedule object representing scheduling rule of given
		 * expression. cron fields previously set by set() method are ignored.
		 */
		public Schedule build(String exp) throws Exception {
			String[] splited = exp.split(" ");
			if (splited.length != 5)
				throw new ParseException("wrong format", 0);

			set(CronField.Type.MINUTE, splited[0]);
			set(CronField.Type.HOUR, splited[1]);
			set(CronField.Type.DAY_OF_MONTH, splited[2]);
			set(CronField.Type.MONTH, splited[3]);
			set(CronField.Type.DAY_OF_WEEK, splited[4]);
			return build();
		}

		/**
		 * same as build("0 0 1 1 *")
		 */
		public Schedule buildYearly() {
			return mustSuccessBuild("0 0 1 1 *");
		}

		/**
		 * same as build("0 0 1 * *")
		 */
		public Schedule buildMonthly() {
			return mustSuccessBuild("0 0 1 * *");
		}

		/**
		 * same as build("0 0 * * 0")
		 */
		public Schedule buildWeekly() {
			return mustSuccessBuild("0 0 * * 0");
		}

		/**
		 * same as build("0 0 * * *")
		 */
		public Schedule buildDaily() {
			return mustSuccessBuild("0 0 * * *");
		}

		/**
		 * same as build("0 * * * *")
		 */
		public Schedule buildHourly() {
			return mustSuccessBuild("0 * * * *");
		}

		private Schedule mustSuccessBuild(String expression) {
			try {
				return build(expression);
			} catch (Exception e) {
				// ignore all, not reachable
				return null;
			}
		}
	}

}

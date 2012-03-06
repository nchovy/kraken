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
package org.krakenapps.cron.test;

import java.text.ParseException;

import org.junit.Test;
import org.krakenapps.cron.Schedule;
import org.krakenapps.cron.impl.CronField;
import org.osgi.framework.InvalidSyntaxException;

import static org.junit.Assert.*;

public class ScheduleBuildTest {
	@Test
	public void testBuildManual() throws Exception {
		try {
			Schedule test = new Schedule.Builder("daily").set(CronField.Type.DAY_OF_WEEK, "0").set(
					CronField.Type.DAY_OF_MONTH, "*/10").set(CronField.Type.HOUR, "4-10").set(CronField.Type.MINUTE,
					"0,3,59").set(CronField.Type.MONTH, "12").build();
			Schedule test2 = new Schedule.Builder("daily").set(CronField.Type.DAY_OF_WEEK, "3").set(
					CronField.Type.DAY_OF_MONTH, "1").set(CronField.Type.HOUR, "0-23").set(CronField.Type.MINUTE,
					"2,3,3").build();

			System.out.println("test1" + test);
			System.out.println("test2" + test2);

			assertEquals(test.fieldMembers(CronField.Type.MINUTE), "{0, 3, 59}");
			assertEquals(test.fieldMembers(CronField.Type.DAY_OF_MONTH), "{0, 10, 20, 30}"); // interval
			assertEquals(test.fieldMembers(CronField.Type.HOUR), "{4, 5, 6, 7, 8, 9, 10}"); // range
			assertEquals(test.fieldMembers(CronField.Type.MONTH), "{11}");

			assertEquals(test2.fieldMembers(CronField.Type.MINUTE), "{2, 3}"); // specify
			assertEquals(test2.fieldMembers(CronField.Type.MONTH), "{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}"); // default

			assertEquals(test, new Schedule.Builder("daily").build("0,3,59 4-10 */10 12 0"));
		} catch (Exception e) {
			fail();
			// TODO: handle exception
		}
	}

	@Test
	public void testBuildString() throws InvalidSyntaxException, Exception {
		try {
			Schedule test3 = new Schedule.Builder("daily").build("0 0 1 1 3-6");
			System.out.println("test3" + test3);

			assertEquals(test3.fieldMembers(CronField.Type.DAY_OF_WEEK), "{3, 4, 5, 6}");
			assertEquals(test3.fieldMembers(CronField.Type.DAY_OF_MONTH), "{0}");
			assertEquals(test3.fieldMembers(CronField.Type.MONTH), "{0}");
		} catch (Exception e) {
			fail();
		}

	}

	@Test
	public void testBuildShort() throws InvalidSyntaxException, Exception {
		try {
			Schedule test8 = new Schedule.Builder("daily").build();
			Schedule test4 = new Schedule.Builder("daily").buildYearly();
			Schedule test5 = new Schedule.Builder("daily").buildMonthly();
			Schedule test6 = new Schedule.Builder("daily").buildDaily();
			Schedule test7 = new Schedule.Builder("daily").buildWeekly();
			System.out.println("test8" + test8);
			System.out.println("test4" + test4);
			System.out.println("test5" + test5);
			System.out.println("test6" + test6);
			System.out.println("test7" + test7);

			assertEquals(test8, new Schedule.Builder("daily").build("* * * * * "));
			assertEquals(test4, new Schedule.Builder("daily").build("0 0 1 1 *"));
			assertEquals(test5, new Schedule.Builder("daily").build("0 0 1 * *"));
			assertEquals(test6, new Schedule.Builder("daily").build("0 0 * * *"));
			assertEquals(test7, new Schedule.Builder("daily").build("0 0 * * 0"));
		} catch (Exception e) {
			fail();
			// TODO: handle exception
		}

	}

	@Test
	public void testSafety() throws Exception {
		try {
			Schedule.Builder safe_builder = new Schedule.Builder("daily");
			Schedule safe_test = safe_builder.build();
			System.out.println("safe_test1" + safe_test);
			safe_builder.set(CronField.Type.HOUR, "3");
			System.out.println("safe_test2" + safe_test);
			assertEquals(safe_test, new Schedule.Builder("daily").build());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testCollision() {
		try {
			Schedule test8 = new Schedule.Builder("daily").build();
			Schedule test9 = new Schedule.Builder("daily").set(CronField.Type.DAY_OF_WEEK, "*").build();
			Schedule test10 = new Schedule.Builder("daily").set(CronField.Type.DAY_OF_WEEK, "3").build();
			System.out.println("test8" + test8);
			System.out.println("test9" + test9);
			System.out.println("test10" + test10);

			assertEquals(test8.fieldMembers(CronField.Type.DAY_OF_WEEK), "{}");
			assertEquals(test9.fieldMembers(CronField.Type.DAY_OF_WEEK), "{}");
			assertEquals(test10.fieldMembers(CronField.Type.DAY_OF_MONTH), "{}");
			assertEquals(test10.fieldMembers(CronField.Type.DAY_OF_WEEK), "{3}");
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testException() {
		try {
			new Schedule.Builder("daily").set(CronField.Type.MONTH, "0");
			fail();
		} catch (ParseException e) {
		}
		try {
			new Schedule.Builder("daily").set(CronField.Type.DAY_OF_MONTH, "0");
			fail();
		} catch (ParseException e) {
		}
		try {
			new Schedule.Builder("daily").set(CronField.Type.HOUR, "24");
			fail();
		} catch (ParseException e) {
		}
		try {
			new Schedule.Builder("daily").set(CronField.Type.HOUR, "-4");
			fail();
		} catch (ParseException e) {
		}
		try {
			new Schedule.Builder("daily").build("0 0 0 0");
			fail();
		} catch (ParseException e) {
		} catch (Exception e) {
		}
	}
}

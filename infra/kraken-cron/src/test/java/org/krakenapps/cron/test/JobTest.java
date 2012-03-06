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

import java.util.Calendar;
import java.util.Date;

import org.krakenapps.cron.Schedule;
import org.krakenapps.cron.impl.CronField;
import org.krakenapps.cron.impl.Job;
import org.osgi.framework.InvalidSyntaxException;

import org.junit.Test;
import static org.junit.Assert.*; 

public class JobTest {
	@Test
	public void testIsTimeToDo() {
		try {
			Schedule sche1 = new Schedule.Builder("job").build();
			Job job1 = new Job(21, sche1);
			assertEquals(job1.isTimeToDo(), true);

			Schedule sche2 = new Schedule.Builder("job").build("*/3 * * * *");
			System.out.println(sche2.fieldMembers(CronField.Type.MINUTE));
			Job job2 = new Job(21, sche2);

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 2);
			Date date = cal.getTime();
			job2.setNextOccurence(date);
			cal.set(Calendar.MINUTE, 0);
			date = cal.getTime();

			assertFalse(job2.isTimeToDo(date));// not yet

			cal.set(Calendar.MINUTE, 1);
			date = cal.getTime();
			assertFalse(job2.isTimeToDo(date));// not yet

			cal.set(Calendar.MINUTE, 2);
			date = cal.getTime();
			assertFalse(job2.isTimeToDo(date));// not yet

			cal.set(Calendar.MINUTE, 3);// now
			date = cal.getTime();
			assertTrue(job2.isTimeToDo(date));

			cal.set(Calendar.MINUTE, 4);// still
			date = cal.getTime();
			assertTrue(job2.isTimeToDo(date));

			cal.set(Calendar.SECOND, 20);// time over
			date = cal.getTime();
			assertTrue(job2.isTimeToDo(date));

			cal.set(Calendar.SECOND, 40);// time over
			date = cal.getTime();
			assertTrue(job2.isTimeToDo(date));

		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

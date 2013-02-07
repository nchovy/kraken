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

import static org.junit.Assert.*;

import org.junit.Test;
import org.krakenapps.cron.Schedule;
import org.krakenapps.cron.impl.Job;

public class JobCloneTest {

	@Test
	public void testClone(){
		Job job = new Job(9999, new Schedule.Builder("ttt").build());
		Job clone = job.clone();
//		System.out.println(job);
//		System.out.println(clone);
		System.out.println(job.equals(clone));
		assertEquals(job, clone);
	}
}

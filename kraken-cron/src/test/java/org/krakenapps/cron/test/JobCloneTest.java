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

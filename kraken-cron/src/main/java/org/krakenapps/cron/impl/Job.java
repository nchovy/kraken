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

import java.util.Date;

import org.krakenapps.cron.Schedule;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Job class used as a component of cron scheduler
 * 
 * @author periphery
 * @since 1.0.0
 */
public class Job implements Comparable<Job>, Cloneable {
	public final int scheduleId;
	public final Schedule schedule;
	public Date date;

	public Job(int schduleId, Schedule schedule) {
		this.scheduleId = schduleId;
		this.schedule = schedule;
		this.setNextOccurence();
	}

	public boolean isTimeToDo() {
		return isTimeToDo(new Date());
	}

	/**
	 * return true for jobs whose time passed.
	 */
	public boolean isTimeToDo(Date now) {
		if (now.getTime() / 1000 - date.getTime() / 1000 < 0) {
			return false;
		} else {
			return true;
		}
	}

	public int getScheduleId() {
		return this.scheduleId;
	}

	@Override
	public int compareTo(Job o) {
		return this.date.compareTo(o.date);
	}

	public void setNextOccurence(Date now) {
		this.date = NextOccurenceCalculator.getNextOccurence(schedule, now);
	}

	public void setNextOccurence() {
		this.date = NextOccurenceCalculator.getNextOccurence(schedule, new Date());
	}

	public String toString() {
		return String.format("[%3d] %15s / %8s", this.scheduleId, this.date, this.schedule.getTaskName());
	}

	@Override
	public Job clone() {
		try {
			Job clone = (Job) super.clone();
			clone.date = new Date(this.date.getTime());
			return clone;
		} catch (Exception e) {
			throw new AssertionError();
		}
	}

	public void run() throws NullPointerException, InvalidSyntaxException {
		Runnable task = CronServiceImpl.getRef(this.schedule.getTaskName());
		if (task == null) {
			throw new NullPointerException("runnable not active");
		}
		
		task.run();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((schedule == null) ? 0 : schedule.hashCode());
		result = prime * result + scheduleId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Job other = (Job) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (schedule == null) {
			if (other.schedule != null)
				return false;
		} else if (!schedule.equals(other.schedule))
			return false;
		if (scheduleId != other.scheduleId)
			return false;
		return true;
	}
	
	
	
	
}

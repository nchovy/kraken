package org.krakenapps.cron;

public class DuplicatedScheduleException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private Schedule schedule;

	public DuplicatedScheduleException(Schedule schedule) {
		this.schedule = schedule;
	}

	public Schedule getSchedule() {
		return schedule;
	}
}

package org.krakenapps.dom.model;

public class Schedule {
	private int dayOfWeek;
	private int beginSecond;
	private int endSecond;

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public int getBeginSecond() {
		return beginSecond;
	}

	public void setBeginSecond(int beginSecond) {
		if (endSecond < 0 || endSecond > 86400)
			throw new IllegalArgumentException("end second should be 0 <= value <= 86400");
		this.beginSecond = beginSecond;
	}

	public int getEndSecond() {
		return endSecond;
	}

	public void setEndSecond(int endSecond) {
		if (endSecond < 0 || endSecond > 86400)
			throw new IllegalArgumentException("end second should be 0 <= value <= 86400");
		this.endSecond = endSecond;
	}
}

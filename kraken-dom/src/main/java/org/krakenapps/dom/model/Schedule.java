package org.krakenapps.dom.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "dom_schedules")
public class Schedule implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn(name = "timetable_id", nullable = false)
	private Timetable timetable;

	@Column(name = "wday")
	private int dayOfWeek;

	@Column(name = "begin_sec")
	private int beginSecond;

	@Column(name = "end_sec")
	private int endSecond;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Timetable getTimetable() {
		return timetable;
	}

	public void setTimetable(Timetable timetable) {
		this.timetable = timetable;
	}

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

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("day", dayOfWeek);
		m.put("begin", beginSecond);
		m.put("end", endSecond);
		return m;
	}

}

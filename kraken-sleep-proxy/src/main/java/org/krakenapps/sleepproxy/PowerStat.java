package org.krakenapps.sleepproxy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.msgbus.Marshalable;
import org.krakenapps.sleepproxy.model.PowerLog;

public class PowerStat implements Marshalable {
	// 5min boundary
	private Date date;

	// watt-hour unit
	private double used;
	private double canSaved;
	private double saved;

	public PowerStat(Date d) {
		this.date = d;
	}

	public PowerStat(PowerLog log) {
		this.date = log.getDate();

		// convert to watt-hour unit
		this.used = log.getUsed() / 3600.0;
		this.canSaved = log.getCanSaved() / 3600.0;
		this.saved = log.getSaved() / 3600.0;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public double getUsed() {
		return used;
	}

	public void setUsed(double used) {
		this.used = used;
	}

	public double getCanSaved() {
		return canSaved;
	}

	public void setCanSaved(double canSaved) {
		this.canSaved = canSaved;
	}

	public double getSaved() {
		return saved;
	}

	public void setSaved(double saved) {
		this.saved = saved;
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("date", dateFormat.format(date));
		m.put("used", used);
		m.put("can_saved", canSaved);
		m.put("saved", saved);
		return m;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String d = dateFormat.format(date);
		return "date=" + d + ", used=" + used + ", canSaved=" + canSaved + ", saved=" + saved;
	}
}

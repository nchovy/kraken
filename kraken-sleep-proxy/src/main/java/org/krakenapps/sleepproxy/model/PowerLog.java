package org.krakenapps.sleepproxy.model;

import java.text.SimpleDateFormat;
import java.util.Date;
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
@Table(name = "sproxy_power_logs")
public class PowerLog implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn(name = "agent_id", nullable = false)
	private Agent agent;

	@Column(nullable = false)
	private Date date;

	// watt-seconds units
	@Column(nullable = false)
	private int used;

	@Column(name = "can_saved", nullable = false)
	private int canSaved;

	@Column(nullable = false)
	private int saved;
	
	public PowerLog() {
	}
	
	public PowerLog(Date date, int used, int canSaved, int saved) {
		this.date = date;
		this.used = used;
		this.canSaved = canSaved;
		this.saved = saved;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getUsed() {
		return used;
	}

	public void setUsed(int used) {
		this.used = used;
	}

	public int getCanSaved() {
		return canSaved;
	}

	public void setCanSaved(int canSaved) {
		this.canSaved = canSaved;
	}

	public int getSaved() {
		return saved;
	}

	public void setSaved(int saved) {
		this.saved = saved;
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("agent_id", agent.getId());
		m.put("date", dateFormat.format(date));
		m.put("used", used);
		m.put("can_saved", canSaved);
		m.put("saved", saved);
		return m;
	}

}

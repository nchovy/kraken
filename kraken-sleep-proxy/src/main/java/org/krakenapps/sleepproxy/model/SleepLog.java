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
@Table(name = "sproxy_sleep_logs")
public class SleepLog implements Marshalable {
	public enum Status {
		None(0), Idle(1), Busy(2), Suspend(3), Resume(4), Heartbeat(5);

		Status(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static Status parse(int code) {
			switch (code) {
			case 1:
				return Idle;
			case 2:
				return Busy;
			case 3:
				return Suspend;
			case 4:
				return Resume;
			case 5:
				return Heartbeat;
			default:
				return None;
			}
		}

		private int code;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn(name = "agent_id")
	private Agent agent;

	@Column(length = 20, name = "host_name", nullable = false)
	private String hostName;

	@Column(nullable = false)
	private int status;

	@Column(length = 255)
	private String params;

	@Column(name = "created_at", nullable = false)
	private Date created;

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

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Status getStatus() {
		return Status.parse(status);
	}

	public void setStatus(Status status) {
		this.status = status.getCode();
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("hostname", hostName);
		m.put("status", getStatus());
		m.put("params", params);
		m.put("created_at", dateFormat.format(created));
		return m;
	}

	@Override
	public String toString() {
		return String.format("id=%d, hostname=%s, status=%d", id, hostName, status);
	}

}

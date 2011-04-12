package org.krakenapps.sleepproxy.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "sproxy_agents", uniqueConstraints = { @UniqueConstraint(columnNames = { "guid" }) })
public class Agent implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "group_id")
	private AgentGroup agentGroup;

	@Column(length = 40, nullable = false)
	private String guid;

	@Column(length = 60, name = "user_name", nullable = false)
	private String userName;

	@Column(length = 20, name = "host_name", nullable = false)
	private String hostName;

	@Column(length = 20, name = "domain_name", nullable = false)
	private String domainName;

	@Column(name = "created_at", nullable = false)
	private Date created;

	@Column(name = "updated_at", nullable = false)
	private Date updated;

	@Column(name = "power")
	private Integer powerConsumtion;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "agent", orphanRemoval = false)
	private List<SleepLog> logs = new ArrayList<SleepLog>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "agent")
	private List<PowerLog> powerLogs = new ArrayList<PowerLog>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "agent", fetch = FetchType.EAGER)
	private List<NetworkAdapter> adapters = new ArrayList<NetworkAdapter>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public AgentGroup getAgentGroup() {
		return agentGroup;
	}

	public void setAgentGroup(AgentGroup agentGroup) {
		this.agentGroup = agentGroup;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public Integer getPowerConsumtion() {
		return powerConsumtion;
	}

	public void setPowerConsumtion(Integer powerConsumtion) {
		this.powerConsumtion = powerConsumtion;
	}

	public List<SleepLog> getSleepLogs() {
		return logs;
	}

	public void setSleepLogs(List<SleepLog> logs) {
		this.logs = logs;
	}

	public List<PowerLog> getPowerLogs() {
		return powerLogs;
	}

	public void setPowerLogs(List<PowerLog> powerLogs) {
		this.powerLogs = powerLogs;
	}

	public List<NetworkAdapter> getAdapters() {
		return adapters;
	}

	public void setAdapters(List<NetworkAdapter> adapters) {
		this.adapters = adapters;
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("guid", guid);
		m.put("group_id", agentGroup.getId());
		m.put("group_name", agentGroup.getName());
		m.put("username", userName);
		m.put("hostname", hostName);
		m.put("domain", domainName);
		m.put("power_consumption", powerConsumtion);
		m.put("created_at", dateFormat.format(created));
		m.put("updated_at", dateFormat.format(updated));
		return m;
	}

}

package org.krakenapps.sleepproxy.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "sproxy_agent_groups")
public class AgentGroup implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String name;

	private String description;

	@ManyToOne
	@JoinColumn(name = "policy_id")
	private SleepPolicy policy;

	@ManyToOne
	@JoinColumn(name = "parent", nullable = true)
	private AgentGroup parent;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
	private List<AgentGroup> children;

	@Column(name = "created_at")
	private Date created;

	@Column(name = "updated_at")
	private Date updated;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "agentGroup")
	private List<Agent> agents;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public SleepPolicy getPolicy() {
		return policy;
	}

	public void setPolicy(SleepPolicy policy) {
		this.policy = policy;
	}

	public AgentGroup getParent() {
		return parent;
	}

	public void setParent(AgentGroup parent) {
		this.parent = parent;
	}

	public List<AgentGroup> getChildren() {
		return children;
	}

	public void setChildren(List<AgentGroup> children) {
		this.children = children;
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

	public List<Agent> getAgents() {
		return agents;
	}

	public void setAgents(List<Agent> agents) {
		this.agents = agents;
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("parent_id", parent != null ? parent.getId() : null);
		m.put("name", name);
		m.put("description", description);
		m.put("policy_id", policy.getId());
		m.put("policy_name", policy.getName());
		m.put("description", description);
		m.put("created_at", dateFormat.format(created));
		m.put("updated_at", dateFormat.format(updated));
		return m;
	}
}

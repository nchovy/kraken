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
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "sproxy_sleep_policies")
public class SleepPolicy implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String name;

	private String description;

	@Column(name = "away")
	private int awayCriteria;

	@Column(name = "force_hibernate")
	private int forceHibernate;

	@Column(name = "created_at", nullable = false)
	private Date created;

	@Column(name = "updated_at", nullable = false)
	private Date updated;

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

	public int getAwayCriteria() {
		return awayCriteria;
	}

	public void setAwayCriteria(int awayCriteria) {
		this.awayCriteria = awayCriteria;
	}

	public boolean getForceHibernate() {
		return forceHibernate != 0;
	}

	public void setForceHibernate(boolean forceHibernate) {
		this.forceHibernate = forceHibernate ? 1 : 0;
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

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("name", name);
		m.put("description", description);
		m.put("away_criteria", awayCriteria);
		m.put("force_hibernate", getForceHibernate());
		m.put("created_at", dateFormat.format(created));
		m.put("updated_at", dateFormat.format(updated));

		return m;
	}
}

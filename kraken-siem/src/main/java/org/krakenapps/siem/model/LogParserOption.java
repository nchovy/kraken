package org.krakenapps.siem.model;

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
@Table(name = "siem_log_parser_options")
public class LogParserOption implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn(name = "logger_id")
	private ManagedLogger managedLogger;

	@Column(length = 60, nullable = false)
	private String name;

	@Column(length = 250)
	private String value;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ManagedLogger getManagedLogger() {
		return managedLogger;
	}

	public void setManagedLogger(ManagedLogger managedLogger) {
		this.managedLogger = managedLogger;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("logger_id", managedLogger.getId());
		m.put("name", name);
		m.put("value", value);
		return m;
	}

}

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
@Table(name = "dom_admin_trust_hosts")
public class AdminTrustHost implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn(name = "admin_id", nullable = false)
	private Admin admin;

	@Column(nullable = false, length = 20)
	private String ip;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Admin getAdmin() {
		return admin;
	}

	public void setAdmin(Admin admin) {
		this.admin = admin;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("ip", ip);
		return m;
	}
}

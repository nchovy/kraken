package org.krakenapps.ipmanager.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ipm_denied_macs")
public class DeniedMac {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn(name = "ip_id", nullable = false)
	private IpEntry ip;

	@Column(length = 20, nullable = false)
	private String mac;

	@Column(name = "begin_at")
	private Date beginDateTime;

	@Column(name = "end_at")
	private Date endDateTime;

	@Column(name = "created_at", nullable = false)
	private Date createDateTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public IpEntry getIp() {
		return ip;
	}

	public void setIp(IpEntry ip) {
		this.ip = ip;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public Date getBeginDateTime() {
		return beginDateTime;
	}

	public void setBeginDateTime(Date beginDateTime) {
		this.beginDateTime = beginDateTime;
	}

	public Date getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

}

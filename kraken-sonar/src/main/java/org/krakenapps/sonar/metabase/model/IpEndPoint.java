package org.krakenapps.sonar.metabase.model;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.krakenapps.pcap.decoder.ethernet.MacAddress;

@Entity
//@Table(name = "sonar_ip_endpoints", uniqueConstraints = { @UniqueConstraint(columnNames = { "mac" }) })
@Table(name = "sonar_ip_endpoints")
public class IpEndPoint {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(nullable = true)
	private String mac;

	@Column(nullable = true)
	private String ip;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "env_id")
	private Environment environment;

	@ManyToMany
	private Set<Application> applications;
		
	public IpEndPoint() {
	}

	public IpEndPoint(MacAddress mac) {
		this.mac = mac.toString();
	}

	public IpEndPoint(InetSocketAddress localAddress) {
		this.ip = localAddress.toString();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public MacAddress getMac() {
		return new MacAddress(mac);
	}

	public void setMac(MacAddress mac) {
		this.mac = mac.toString();
	}

	public InetAddress getIp() {
		try {
			return InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	public void setIp(InetAddress ip) {
		this.ip = ip.getHostAddress();
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public void setApplications(Set<Application> applications) {
		this.applications = applications;
	}
	
	@Override
	public String toString() {
		return String.format("mac=%s, ip=%s, env=%s", mac, ip, environment);
	}
}
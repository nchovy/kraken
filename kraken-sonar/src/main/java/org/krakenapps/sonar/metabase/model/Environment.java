package org.krakenapps.sonar.metabase.model;

import java.util.List;

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

@Entity
@Table(name = "sonar_environments")
public class Environment {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "vendor_id", nullable = false)
	private Vendor vendor;

	@Column(nullable = false)
	private String family;

	@Column(nullable = false)
	private String description;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "environment")
	private List<IpEndPoint> endpoints;
	
	public Environment() {
	}
	
	public Environment(Vendor vendor, String family, String description) {
		this.vendor = vendor;
		this.family = family;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<IpEndPoint> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(List<IpEndPoint> endpoints) {
		this.endpoints = endpoints;
	}

	@Override
	public String toString() {
		return description;
	}

}

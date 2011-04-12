/*
 * Copyright 2011 Future Systems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.dom.model;

import java.util.ArrayList;
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
@Table(name = "dom_host_types")
public class HostType implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn(name = "vendor_id", nullable = false)
	private Vendor vendor;

	@Column(nullable = false, length = 60)
	private String name;

	@Column(nullable = false, length = 60)
	private String version;

	@Column(name = "sentry_support", nullable = false)
	private boolean isSentrySupported;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "pk.type")
	private List<DefaultHostExtension> defaultExtensions;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "hostType")
	private List<Host> hosts = new ArrayList<Host>();

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isSentrySupported() {
		return isSentrySupported;
	}

	public void setSentrySupported(boolean isSentrySupported) {
		this.isSentrySupported = isSentrySupported;
	}

	public List<DefaultHostExtension> getDefaultExtensions() {
		return defaultExtensions;
	}

	public void setDefaultExtensions(List<DefaultHostExtension> defaultExtensions) {
		this.defaultExtensions = defaultExtensions;
	}

	public List<Host> getHosts() {
		return hosts;
	}

	public void setHosts(List<Host> hosts) {
		this.hosts = hosts;
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("name", name);
		m.put("version", version);
		m.put("vendor_name", vendor.getName());
		m.put("sentry_support", isSentrySupported);
		return m;
	}

}

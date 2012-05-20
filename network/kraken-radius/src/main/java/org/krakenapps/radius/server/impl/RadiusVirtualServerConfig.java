package org.krakenapps.radius.server.impl;

import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;

@CollectionName("virtual_servers")
public class RadiusVirtualServerConfig {
	@FieldOption(nullable = false)
	private String name;

	@FieldOption(nullable = false)
	private String profile;

	@FieldOption(nullable = true)
	private String hostName;

	@FieldOption(nullable = true)
	private Integer port;

	@FieldOption(nullable = false)
	private String portType;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getPortType() {
		return portType;
	}

	public void setPortType(String portType) {
		this.portType = portType;
	}

}

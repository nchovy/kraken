package org.krakenapps.webfx.impl;

import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.servlet.api.ServletRegistry;
import org.krakenapps.webfx.WebApplication;
import org.krakenapps.webfx.WebApplicationRegistry;

@Component(name = "webfx-dispatcher-servlet")
public class WebApplicationRegistryImpl implements WebApplicationRegistry {
	@Requires
	private ServletRegistry servletRegistry;

	@Validate
	public void start() {

	}

	@Invalidate
	public void stop() {

	}

	@Override
	public List<WebApplication> getWebApplications() {
		return null;
	}

	@Override
	public WebApplication getWebApplication(String name) {
		return null;
	}

	@Override
	public void createWebApplication(WebApplication app) {
	}

	@Override
	public void updateWebApplication(WebApplication app) {
	}

	@Override
	public void removeWebApplication(String name) {
	}

	@Override
	public void reload(String name) {
	}

}

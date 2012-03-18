package org.krakenapps.webfx;

import java.util.List;

public interface WebApplicationRegistry {
	List<WebApplication> getWebApplications();

	WebApplication getWebApplication(String name);

	void createWebApplication(WebApplication app);

	void updateWebApplication(WebApplication app);

	void removeWebApplication(String name);

	void reload();

}

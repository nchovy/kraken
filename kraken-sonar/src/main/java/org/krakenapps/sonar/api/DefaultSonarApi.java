package org.krakenapps.sonar.api;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.sonar.SonarApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sonar-api")
@Provides
@JpaConfig(factory = "sonar")
public class DefaultSonarApi implements SonarApi {
	private final Logger logger = LoggerFactory.getLogger(DefaultSonarApi.class.getName());

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;
}
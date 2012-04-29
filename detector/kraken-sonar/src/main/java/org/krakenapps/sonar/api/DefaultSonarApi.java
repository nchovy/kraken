package org.krakenapps.sonar.api;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.sonar.SonarApi;

@Component(name = "sonar-api")
@Provides
@JpaConfig(factory = "sonar")
public class DefaultSonarApi implements SonarApi {
}
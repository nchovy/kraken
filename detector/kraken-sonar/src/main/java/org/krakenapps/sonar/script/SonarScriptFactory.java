package org.krakenapps.sonar.script;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.jpa.JpaService;
import org.krakenapps.sonar.Metabase;
import org.krakenapps.sonar.PassiveScanner;
import org.osgi.framework.BundleContext;

@Component(name = "sonar-script-factory")
@Provides
public class SonarScriptFactory implements ScriptFactory {
	private BundleContext bc;

	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "sonar")
	private String alias;

	@Requires
	private PassiveScanner scanner;

	@Requires
	private JpaService jpa;

	@Requires(optional = true, nullable = false)
	private Metabase metabase;

	public SonarScriptFactory(BundleContext bc) {
		this.bc = bc;
	}

	@Override
	public Script createScript() {
		return new SonarScript(bc, scanner, jpa, metabase);
	}

}

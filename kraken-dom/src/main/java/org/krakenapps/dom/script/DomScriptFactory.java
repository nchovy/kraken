package org.krakenapps.dom.script;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.jpa.JpaService;
import org.osgi.framework.BundleContext;

@Component(name = "dom-script-factory")
@Provides
public class DomScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "dom")
	private String alias;

	private BundleContext bc;

	@Requires
	private JpaService jpa;

	public DomScriptFactory(BundleContext bc) {
		this.bc = bc;
	}

	@Override
	public Script createScript() {
		return new DomScript(bc, jpa);
	}

}

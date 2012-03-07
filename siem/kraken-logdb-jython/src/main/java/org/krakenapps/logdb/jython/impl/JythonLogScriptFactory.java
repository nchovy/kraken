package org.krakenapps.logdb.jython.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.logdb.jython.JythonLogScriptRegistry;
import org.osgi.framework.BundleContext;

@Component(name = "jython-logscript-factory")
@Provides
public class JythonLogScriptFactory implements ScriptFactory {

	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "logdb-jython")
	private String alias;

	@Requires
	private JythonLogScriptRegistry ls;

	private BundleContext bc;

	public JythonLogScriptFactory(BundleContext bc) {
		this.bc = bc;
	}

	@Override
	public Script createScript() {
		return new JythonLogScript(bc, ls);
	}

}

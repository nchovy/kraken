package org.krakenapps.report;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;

@Component(name = "birt-script-factory")
@Provides
public class BirtScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "birt")
	private String alias;

	@Requires
	private ReportPrintMachine machine;

	@Override
	public Script createScript() {
		return BirtScript.newInstance(machine);
	}

}

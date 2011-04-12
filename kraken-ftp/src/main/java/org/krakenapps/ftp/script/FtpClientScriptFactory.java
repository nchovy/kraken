package org.krakenapps.ftp.script;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.ftp.FtpProfileService;

@Component(name = "ftp-client-script-factory")
@Provides
public class FtpClientScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "ftp")
	private String alias;
	
	@Requires
	private FtpProfileService service;

	@Override
	public Script createScript() {
		return new FtpClientScript(service);
	}

}

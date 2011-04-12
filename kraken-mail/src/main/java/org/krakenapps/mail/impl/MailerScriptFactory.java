package org.krakenapps.mail.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.mail.MailerRegistry;

@Component(name = "mailer-script-factory")
@Provides
public class MailerScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "mailer")
	private String alias;

	@Requires
	private MailerRegistry registry;
	
	@Override
	public Script createScript() {
		return new MailerScript(registry);
	}

}

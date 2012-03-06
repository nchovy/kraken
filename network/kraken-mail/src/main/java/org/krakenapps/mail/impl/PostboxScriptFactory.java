package org.krakenapps.mail.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.mail.PostboxRegistry;

@Component(name = "postbox-script-factory")
@Provides
public class PostboxScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "postbox")
	private String alias;

	@Requires
	private PostboxRegistry postbox;

	@Override
	public Script createScript() {
		return new PostboxScript(postbox);
	}

}

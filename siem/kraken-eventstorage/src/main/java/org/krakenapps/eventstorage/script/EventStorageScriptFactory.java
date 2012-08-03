package org.krakenapps.eventstorage.script;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.eventstorage.EventStorage;
import org.krakenapps.eventstorage.EventTableRegistry;

@Component(name = "eventstorage-script-factory")
@Provides
public class EventStorageScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "eventstorage")
	private String alias;

	@Requires
	private ConfigService confsvc;

	@Requires
	private EventTableRegistry tableRegistry;

	@Requires
	private EventStorage storage;

	@Override
	public Script createScript() {
		return new EventStorageScript(confsvc, tableRegistry, storage);
	}
}

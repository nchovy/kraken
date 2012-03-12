package org.krakenapps.dom.api;

import org.krakenapps.dom.model.Host;

public interface HostUpdateApi {

	void update(Host host);
	
	int getPendingCount();
}

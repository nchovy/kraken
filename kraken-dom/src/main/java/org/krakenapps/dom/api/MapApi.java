package org.krakenapps.dom.api;

import java.util.Collection;

import org.krakenapps.dom.model.MapInfo;

public interface MapApi extends EntityEventProvider<MapInfo> {
	Collection<MapInfo> getMaps(String domain);

	MapInfo findMap(String domain, String guid);

	MapInfo getMap(String domain, String guid);

	void createMap(String domain, MapInfo map);

	void updateMap(String domain, MapInfo map);

	void removeMap(String domain, String guid);
}

package org.krakenapps.dom.api;

import java.util.Collection;

import org.krakenapps.dom.model.MapInfo;

public interface MapApi extends EntityEventProvider<MapInfo> {
	Collection<MapInfo> getMaps(String domain);

	MapInfo findMap(String domain, String guid);

	MapInfo getMap(String domain, String guid);

	void createMaps(String domain, Collection<MapInfo> maps);

	void createMap(String domain, MapInfo map);

	void updateMaps(String domain, Collection<MapInfo> maps);

	void updateMap(String domain, MapInfo map);

	void removeMaps(String domain, Collection<String> guids);

	void removeMap(String domain, String guid);
}

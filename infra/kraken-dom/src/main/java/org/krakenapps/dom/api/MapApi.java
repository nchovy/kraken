/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

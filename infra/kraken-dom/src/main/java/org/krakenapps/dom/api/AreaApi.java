/*
 * Copyright 2011 Future Systems, Inc.
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

import org.krakenapps.dom.model.Area;

public interface AreaApi extends EntityEventProvider<Area> {
	Collection<Area> getRootAreas(String domain);

	Area findArea(String domain, String guid);

	Area getArea(String domain, String guid);

	void createAreas(String domain, Collection<Area> areas);

	void createArea(String domain, Area area);

	void updateAreas(String domain, Collection<Area> areas);

	void updateArea(String domain, Area area);

	void removeAreas(String domain, Collection<String> guids);

	void removeArea(String domain, String guid);

	void removeAreas(String domain, Collection<String> guids, boolean removeHost);

	void removeArea(String domain, String guid, boolean removeHost);
}

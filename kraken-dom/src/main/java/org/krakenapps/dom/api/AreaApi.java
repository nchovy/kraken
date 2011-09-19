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

import org.krakenapps.dom.exception.AreaNotFoundException;
import org.krakenapps.dom.exception.UndeletableAreaException;
import org.krakenapps.dom.model.Area;

public interface AreaApi extends EntityEventProvider<Area> {
	Collection<Area> getAllAreas();

	Collection<Area> getAllRootAreas();

	Area getRootArea(int organizationId);

	Area getArea(int organizationId, int areaId);

	Collection<Area> getSubAreas(int organizationId, int areaId);

	Area moveArea(int organizationId, int newParentId, int id) throws AreaNotFoundException;

	Area createArea(int organizationId, int parentId, String name, String description) throws AreaNotFoundException;

	Area updateArea(int organizationId, int areaId, String name, String description) throws AreaNotFoundException;

	Area removeArea(int organizationId, int areaId) throws AreaNotFoundException, UndeletableAreaException;
}

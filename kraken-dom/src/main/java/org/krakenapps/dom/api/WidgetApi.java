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

import java.util.List;

import org.krakenapps.dom.exception.AdminNotFoundException;
import org.krakenapps.dom.exception.WidgetConfigNotFoundException;
import org.krakenapps.dom.exception.WidgetNotFoundException;
import org.krakenapps.dom.model.Widget;
import org.krakenapps.dom.model.WidgetConfig;

public interface WidgetApi extends EntityEventProvider<Widget> {
	List<Widget> getWidgets(int userId);

	Widget createWidget(int organizationId, int userId, int programId) throws AdminNotFoundException;

	Widget removeWidget(int userId, int widgetId) throws WidgetNotFoundException;
	
	WidgetConfig setConfig(int userId, int widgetId, String key, String value) throws WidgetNotFoundException;

	WidgetConfig unsetConfig(int userId, int widgetId, String key) throws WidgetConfigNotFoundException;
}

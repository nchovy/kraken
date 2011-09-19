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
package org.krakenapps.dom.api.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.WidgetApi;
import org.krakenapps.dom.exception.AdminNotFoundException;
import org.krakenapps.dom.exception.WidgetConfigNotFoundException;
import org.krakenapps.dom.exception.WidgetNotFoundException;
import org.krakenapps.dom.model.Program;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.Widget;
import org.krakenapps.dom.model.WidgetConfig;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-widget-api")
@Provides
@JpaConfig(factory = "dom")
public class WidgetApiImpl extends AbstractApi<Widget> implements WidgetApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Requires
	private ProgramApi programApi;

	@Requires
	private AdminApi adminApi;

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<Widget> getWidgets(int adminId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM Widget w WHERE w.admin.id = ?").setParameter(1, adminId).getResultList();
	}

	@Override
	public Widget createWidget(int organizationId, int adminId, int programId) throws AdminNotFoundException {
		Widget widget = createWidgetInternal(organizationId, adminId, programId);
		fireEntityAdded(widget);
		return widget;
	}

	@Transactional
	private Widget createWidgetInternal(int organizationId, int adminId, int programId) {
		EntityManager em = entityManagerService.getEntityManager();
		Admin admin = adminApi.getAdmin(organizationId, adminId);
		if (admin == null)
			throw new AdminNotFoundException(adminId);

		Program program = programApi.getProgram(programId);

		Widget widget = new Widget();
		widget.setAdmin(admin);
		widget.setProgram(program);
		em.persist(widget);
		return widget;
	}

	@Override
	public Widget removeWidget(int adminId, int widgetId) throws WidgetNotFoundException {
		Widget widget = removeWidgetInternal(adminId, widgetId);
		fireEntityRemoved(widget);
		return widget;
	}

	@Transactional
	private Widget removeWidgetInternal(int adminId, int widgetId) {
		EntityManager em = entityManagerService.getEntityManager();
		Widget widget = em.find(Widget.class, widgetId);
		if (widget == null || widget.getAdmin().getId() != adminId)
			throw new WidgetNotFoundException();

		em.remove(widget);
		return widget;
	}

	@Override
	public WidgetConfig setConfig(int adminId, int widgetId, String name, String value) throws WidgetNotFoundException {
		WidgetConfig config = setConfigInternal(adminId, widgetId, name, value);
		Widget widget = config.getWidget();
		widget.getWidgetConfigs().size();
		fireEntityUpdated(widget);
		return config;
	}

	@Transactional
	private WidgetConfig setConfigInternal(int adminId, int widgetId, String name, String value)
			throws WidgetNotFoundException {
		EntityManager em = entityManagerService.getEntityManager();

		WidgetConfig config = findWidgetConfig(em, widgetId, name);
		if (config != null) {
			// update existing config
			config.setValue(value);
			em.merge(config);
		} else {
			// find widget first
			Widget widget = em.find(Widget.class, widgetId);
			if (widget == null || widget.getAdmin().getId() != adminId)
				throw new WidgetNotFoundException();

			config = new WidgetConfig();
			config.setWidget(widget);
			config.setName(name);
			config.setValue(value);

			em.persist(config);
		}
		return config;
	}

	private WidgetConfig findWidgetConfig(EntityManager em, int widgetId, String name) {
		try {
			return (WidgetConfig) em.createQuery("FROM WidgetConfig c WHERE c.widget.id = ? AND c.name = ?")
					.setParameter(1, widgetId).setParameter(2, name).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public WidgetConfig unsetConfig(int adminId, int widgetId, String key) throws WidgetConfigNotFoundException {
		WidgetConfig config = unsetConfigInternal(adminId, widgetId, key);
		Widget widget = config.getWidget();
		widget.getWidgetConfigs().size();
		fireEntityUpdated(widget);
		return config;
	}

	@Transactional
	private WidgetConfig unsetConfigInternal(int adminId, int widgetId, String key)
			throws WidgetConfigNotFoundException {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			WidgetConfig config = (WidgetConfig) em
					.createQuery("FROM WidgetConfig c WHERE c.widget.id = ? AND c.name = ?").setParameter(1, widgetId)
					.setParameter(2, key).getSingleResult();
			if (config.getWidget().getAdmin().getId() != adminId)
				throw new WidgetConfigNotFoundException();

			em.remove(config);
			return config;
		} catch (NoResultException e) {
			throw new WidgetConfigNotFoundException();
		}
	}
}

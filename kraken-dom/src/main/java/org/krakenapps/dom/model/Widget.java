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
package org.krakenapps.dom.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;
import org.krakenapps.msgbus.Marshaler;

@Entity
@Table(name = "dom_widgets")
public class Widget implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	@JoinColumn(name = "admin_id", nullable = false)
	private Admin admin;

	@ManyToOne
	@JoinColumn(name = "program_id", nullable = false)
	private Program program;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "widget")
	private List<WidgetConfig> widgetConfigs = new ArrayList<WidgetConfig>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Admin getAdmin() {
		return admin;
	}

	public void setAdmin(Admin admin) {
		this.admin = admin;
	}

	public Program getProgram() {
		return program;
	}

	public void setProgram(Program program) {
		this.program = program;
	}

	public List<WidgetConfig> getWidgetConfigs() {
		return widgetConfigs;
	}

	public void setWidgetConfigs(List<WidgetConfig> widgetConfigs) {
		this.widgetConfigs = widgetConfigs;
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("admin_id", admin.getId());
		map.put("program_id", program.getId());
		map.put("configs", Marshaler.marshal(widgetConfigs));
		return map;
	}
}

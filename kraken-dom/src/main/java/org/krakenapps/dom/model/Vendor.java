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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "dom_vendors")
public class Vendor implements Marshalable {
	@Id
	private String guid;

	@Column(nullable = false, length = 60, unique = true)
	private String name;

	@Column(name = "created_at", nullable = false)
	private Date createDateTime;

	@Column(name = "updated_at", nullable = false)
	private Date updateDateTime;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "vendor")
	private List<HostType> hostTypes = new ArrayList<HostType>();

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<HostType> getHostTypes() {
		return hostTypes;
	}

	public void setHostTypes(List<HostType> hostTypes) {
		this.hostTypes = hostTypes;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	@Override
	public Map<String, Object> marshal() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("guid", guid);
		m.put("name", name);
		m.put("created_at", dateFormat.format(createDateTime));
		m.put("updated_at", dateFormat.format(updateDateTime));
		return m;
	}
}

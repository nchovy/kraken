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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "dom_programs")
public class Program implements Marshalable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(nullable = false, length = 60)
	private String name;

	@Column(length = 255)
	private String description;

	@Column(name = "type", length = 255)
	private String typeName;

	@ManyToOne
	@JoinColumn(name = "pack_id", nullable = false)
	private ProgramPack pack;

	@Column(nullable = false)
	private boolean visible; // in start menu

	@Column(nullable = false)
	private int seq;

	@ManyToMany(mappedBy = "programs")
	private Set<ProgramProfile> profiles = new HashSet<ProgramProfile>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public ProgramPack getPack() {
		return pack;
	}

	public void setPack(ProgramPack pack) {
		this.pack = pack;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public Set<ProgramProfile> getProgramProfiles() {
		return profiles;
	}

	public void setProgramProfiles(Set<ProgramProfile> profiles) {
		this.profiles = profiles;
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("name", name);
		map.put("description", description);
		map.put("type", typeName);
		map.put("pack_id", pack.getId());
		map.put("visible", visible);
		map.put("seq", seq);
		return map;
	}

}

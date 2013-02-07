/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.word.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Relationship {
	private String id;
	private String type;
	private String target;

	public Relationship() {
	}

	public Relationship(String id, String type, String target) {
		this.id = id;
		this.type = type;
		this.target = target;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@Override
	public String toString() {
		return "Relationship [id=" + id + ", type=" + type + ", target=" + target + "]";
	}

	public Element toXml(Document d) {
		Element e = d.createElement("Relationships");
		return e;
	}
}

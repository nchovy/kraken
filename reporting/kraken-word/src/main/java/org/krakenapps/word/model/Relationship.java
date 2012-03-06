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

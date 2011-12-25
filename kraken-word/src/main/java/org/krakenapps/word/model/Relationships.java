package org.krakenapps.word.model;

import java.util.ArrayList;
import java.util.List;

public class Relationships {
	private List<Relationship> children = new ArrayList<Relationship>();

	public String getNamespace() {
		return "http://schemas.openxmlformats.org/package/2006/relationships";
	}

	public List<Relationship> getChildren() {
		return children;
	}

	public void setChildren(List<Relationship> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return "Relationships [children=" + children + "]";
	}
}

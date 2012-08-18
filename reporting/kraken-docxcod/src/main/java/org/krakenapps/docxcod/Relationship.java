package org.krakenapps.docxcod;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;

public class Relationship {
	public Relationship parent;
	public String id;
	public String target;
	public String type;
	public List<Relationship> children;

	public Relationship() {
		parent = null;
		children = new ArrayList<Relationship>();
	}

	public Relationship(Relationship p, NamedNodeMap m) {
		parent = p;
		id = m.getNamedItem("Id").getNodeValue();
		target = m.getNamedItem("Target").getNodeValue();
		type = m.getNamedItem("Type").getNodeValue();
		children = new ArrayList<Relationship>();
	}
	
	public String toString() {
		if (type != null) {
			String prefix = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/";
			String shortType = type.startsWith(prefix) ? type.substring(prefix.length()) : type;
			if (children.isEmpty())
				return String.format("[%s, %s, %s]", id, target, shortType);
			else 
				return String.format("[%s, %s, %s, %d children]", id, target, shortType, children.size());
		} else {
			return String.format("[root, %d children]", children.size());
		}
		
	}
	
	public String toSummaryString() {
		if (target == null)
			return String.format("%s", toSummaryString(children));
		else {
			if (children == null || children.isEmpty()) {
				return String.format("%s", target);
			} else {
				return String.format("[%s%s]", target, toSummaryString(children));
			}
		}
	}

	private Object toSummaryString(List<Relationship> children) {
		if (children == null)
			return "";
		if (children.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(" [");
			for (Relationship r: children) {
				if (r != children.get(0))
					sb.append(" ");
				sb.append(r.toSummaryString());
			}
			sb.append("]");
			return sb.toString();
		} else {
			return "";
		}
	}
}
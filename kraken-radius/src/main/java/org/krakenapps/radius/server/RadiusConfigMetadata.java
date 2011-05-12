package org.krakenapps.radius.server;

public class RadiusConfigMetadata {
	public enum Type {
		String, Integer, Boolean
	}

	private Type type;
	private String name;
	private boolean isRequired;
	private Object defaultValue;

	public RadiusConfigMetadata(Type type, String name, boolean isRequired) {
		this(type, name, isRequired, null);
	}

	public RadiusConfigMetadata(Type type, String name, boolean isRequired, Object defaultValue) {
		this.type = type;
		this.name = name;
		this.isRequired = isRequired;
		this.defaultValue = defaultValue;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
}

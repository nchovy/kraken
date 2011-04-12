package org.krakenapps.dom.api;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserExtensionSchema {
	private Map<String, FieldDefinition> fields = new HashMap<String, UserExtensionSchema.FieldDefinition>();

	public Collection<String> keySet() {
		return fields.keySet();
	}

	public void register(FieldDefinition field) {
		fields.put(field.getName(), field);
	}

	public void unregister(String name) {
		fields.remove(name);
	}

	public FieldDefinition getField(String name) {
		return fields.get(name);
	}

	public static class FieldDefinition {
		private String name;
		private Map<Locale, String> displayNames = new HashMap<Locale, String>();
		private Map<Locale, String> descriptions = new HashMap<Locale, String>();
		private String type;
		private Object defaultValue;

		public FieldDefinition(String name, String type, Object defaultValue) {
			this.name = name;
			this.type = type;
			this.defaultValue = defaultValue;
			validate(defaultValue);
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		public String getDisplayName(Locale locale) {
			String l = displayNames.get(locale);
			if (l != null)
				return l;

			return displayNames.get(Locale.ENGLISH);
		}

		public Map<Locale, String> getDisplayNames() {
			return displayNames;
		}

		public void setDisplayName(Locale locale, String name) {
			displayNames.put(locale, name);
		}

		public String getDescription(Locale locale) {
			String l = descriptions.get(locale);
			if (l != null)
				return l;

			return descriptions.get(Locale.ENGLISH);
		}

		public Map<Locale, String> getDescriptions() {
			return descriptions;
		}

		public void setDescription(Locale locale, String description) {
			descriptions.put(locale, description);
		}

		public void validate(Object value) {
			if (value instanceof String && type.equalsIgnoreCase("string"))
				return;
			if (value instanceof Integer && type.equalsIgnoreCase("integer"))
				return;
			if (value instanceof Boolean && type.equalsIgnoreCase("boolean"))
				return;
			if (value instanceof Date && type.equalsIgnoreCase("date"))
				return;
			throw new IllegalArgumentException("kraken-dom: " + name + " type does not match");
		}
	}
}

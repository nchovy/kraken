package org.krakenapps.dom.api;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.krakenapps.msgbus.Localizable;
import org.krakenapps.msgbus.Marshaler;

public class UserExtensionSchema implements Localizable {
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

	@Override
	public Map<String, Object> marshal() {
		return Marshaler.marshal(fields);
	}

	@Override
	public Map<String, Object> marshal(Locale locale) {
		return Marshaler.marshal(fields, locale);
	}

	public static class FieldDefinition implements Localizable {
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

		@Override
		public Map<String, Object> marshal() {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("name", name);
			m.put("type", type);
			m.put("default_value", defaultValue);
			m.put("display_names", marshal(displayNames));
			m.put("descriptions", marshal(descriptions));
			return m;
		}

		@Override
		public Map<String, Object> marshal(Locale locale) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("name", name);
			m.put("type", type);
			m.put("default_value", defaultValue);
			m.put("display_name", localize(displayNames, locale));
			m.put("description", localize(descriptions, locale));
			return m;
		}

		private String localize(Map<Locale, String> ls, Locale locale) {
			if (ls.containsKey(locale))
				return ls.get(locale);

			return ls.get(Locale.ENGLISH);
		}

		private Map<String, Object> marshal(Map<Locale, String> ls) {
			Map<String, Object> m = new HashMap<String, Object>();
			for (Locale l : ls.keySet())
				m.put(l.getLanguage(), ls.get(l));
			return m;
		}
	}
}

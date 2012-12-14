package org.krakenapps.dns;

import org.krakenapps.dns.DnsResourceRecord.Clazz;
import org.krakenapps.dns.DnsResourceRecord.Type;

public class DnsCacheKey {
	private String name;
	private Type type;
	private Clazz clazz;

	public DnsCacheKey() {
	}

	public DnsCacheKey(String name, Type type, Clazz clazz) {
		this.name = name;
		this.type = type;
		this.clazz = clazz;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Clazz getClazz() {
		return clazz;
	}

	public void setClazz(Clazz clazz) {
		this.clazz = clazz;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DnsCacheKey other = (DnsCacheKey) obj;
		if (clazz != other.clazz)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "name=" + name + ", type=" + type + ", clazz=" + clazz;
	}
}

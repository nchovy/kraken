package org.krakenapps.confdb.file;

/**
 * collection entry of manifest
 * 
 * @author xeraph
 * 
 */
class CollectionEntry {
	private int id;
	private String name;
	
	public CollectionEntry() {
	}

	public CollectionEntry(int id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		CollectionEntry other = (CollectionEntry) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}

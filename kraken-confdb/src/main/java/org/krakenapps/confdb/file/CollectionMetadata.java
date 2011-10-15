package org.krakenapps.confdb.file;

class CollectionMetadata {
	private int id;
	private String name;

	public CollectionMetadata(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}

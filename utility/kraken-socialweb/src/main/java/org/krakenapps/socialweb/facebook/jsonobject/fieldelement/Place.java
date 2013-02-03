package org.krakenapps.socialweb.facebook.jsonobject.fieldelement;

public class Place {

	private String id;
	private String name;
	private int longitude;
	private int latitude;
	
	public Place(){
	}
	public Place(String id , String name, int lng, int lat){
		this.id = id;
		this.name = name;
		this.longitude = lng;
		this.latitude = lat;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public int getLongitude() {
		return longitude;
	}
	public void setLongitude(int longitude) {
		this.longitude = longitude;
	}
	public int getLatitude() {
		return latitude;
	}
	public void setLatitude(int latitude) {
		this.latitude = latitude;
	}

}

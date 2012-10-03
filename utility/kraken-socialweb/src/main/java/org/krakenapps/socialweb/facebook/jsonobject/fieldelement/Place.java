package org.krakenapps.socialweb.facebook.jsonobject.fieldelement;

public class Place {

	String id;
	String name;
	Location location;
	
	private class Location{
		private int longitude;
		private int latitude;
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
	public Place(){
		location = null;
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

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}

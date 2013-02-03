package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class Event implements FacebookGraphObject{

	private String id;
	private From owner;
	private String name;
	private String description;
	private String start_time;
	private String end_time;
	private String location;
	private Venue venue;
	private String privacy;
	private String updated_time;
	private String picture;
	private FbConnection fbConnection;

	public Event(){
		fbConnection = new FbConnection();
	}
	private class FbConnection{
		public FbConnection(){
			
		}
		public String CONN_feed = "feed";
		public String CONN_noreply = "noreply";
		public String CONN_invited = "invited";
		public String CONN_attending = "attending";
		public String CONN_maybe = "maybe";
		public String CONN_declined = "declined";
		public String CONN_picture = "picture";
		public String CONN_videos = "videos";
		// connection is null
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public From getOwner() {
		return owner;
	}
	public void setOwner(From owner) {
		this.owner = owner;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getStart_time() {
		return start_time;
	}
	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}
	public String getEnd_time() {
		return end_time;
	}
	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public Venue getVenue() {
		return venue;
	}
	public void setVenue(Venue venue) {
		this.venue = venue;
	}
	public String getPrivacy() {
		return privacy;
	}
	public void setPrivacy(String privacy) {
		this.privacy = privacy;
	}
	public String getUpdated_time() {
		return updated_time;
	}
	public void setUpdated_time(String updated_time) {
		this.updated_time = updated_time;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public FbConnection getFbConnection() {
		return fbConnection;
	}
	public void setFbConnection(FbConnection fbConnection) {
		this.fbConnection = fbConnection;
	}
	@Override
	public int parseJson(JSONObject json) {
		try {
			id = json.getString("id");
			JSONObject fromObject = json.getJSONObject("from");
			owner = new From(fromObject.getString("id"), fromObject.getString("name"));
			name = json.getString("name");
			description = json.getString("description");
			start_time = json.getString("start_time");
			end_time = json.getString("end_time");
			location = json.getString("location");
			
			venue = new Venue(); 
			JSONObject venueObject = json.getJSONObject("venue");
			venue.parse(venueObject);
			
			privacy = json.getString("privacy");
			updated_time = json.getString("updated_time");
			picture = json.getString("picture");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	/* (non-Javadoc)
	 * @see org.krakenapps.socialweb.facebook.jsonobject.FacebookGraphObject#parseJson(org.json.JSONObject, java.util.Set)
	 */
	@Override
	public int parseJson(JSONObject json, Set<Permissions> permit) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}

class Venue{
	private String id;
	private String street;
	private String city;
	private String state;
	private String zip;
	private String country;
	private String latitude;
	private String longitude;
	public void parse(JSONObject json){
		try {
			id = json.getString("id");
			street = json.getString("street");
			city = json.getString("city");
			state = json.getString("state");
			zip = json.getString("zip");
			country = json.getString("country");
			latitude = json.getString("latitude");
			longitude = json.getString("longitude");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
}
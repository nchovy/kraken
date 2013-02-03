package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.Achievement;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.Like;


public class User implements FacebookGraphObject{
	
	private String id;
	private String name;
	private String first_name;
	private String middle_name;
	private String last_name;
	private String gender;
	private String locale;
	private ArrayList<From> languages;
	private String link;
	private String username;
	private String third_party_id;
	private boolean installed;
	private int timezone;
	private String updated_time;
	private boolean verified;
	private String bio;
	private String birthday;
	private ArrayList<Cover> cover;
	private ArrayList<Devices> devices;
	//education
	private String email;
	private From hometown;
	private ArrayList<String> interested_in;
	private From location;
	private String political;
	//payment_pricepoints;
	private ArrayList<From> favorite_athletes; 
	private ArrayList<From> favorite_team;
	private String picture;
	private String quotes;
	private String relationship_status;
	private String religion;
	private String security_settings;
	private From significant_other;
	//video_upload_limits;
	private String website;
	private ArrayList<Work> work;
	private FbConnection fbConnection;
	
	public User(){
		fbConnection = new FbConnection();
	}
	private class FbConnection{
		public String CONN_accounts = "accounts";
		public String CONN_achievements = "achievements";
		public String CONN_activities = "activities";
		public String CONN_albums = "albums";
		public String CONN_apprequests = "apprequests";
		public String CONN_books = "books";
		public String CONN_checkins = "checkins";
		public String CONN_events = "events";
		public String CONN_family = "family";
		public String CONN_feed = "feed";
		public String CONN_friendlists = "friendlists";
		public String CONN_friendrequests = "friendrequests";
		public String CONN_friends = "friends";
		public String CONN_games = "games";
		public String CONN_home = "home";
		public String CONN_inbox = "inbox";
		public String CONN_interests = "interests";
		public String CONN_likes = "likes";
		public String CONN_locations = "locations";
		public String CONN_movies = "movies";
		public String CONN_music = "music";
		public String CONN_mutualfriends = "mutualfriends";
		public String CONN_notes = "notes";
		public String CONN_notifications = "notifications";
		public String CONN_outbox = "outbox";
		public String CONN_payments = "payments";
		public String CONN_permissions = "permissions";
		public String CONN_photos = "photos";
		public String CONN_photos_uploaded = "photos/uploaded";
		public String CONN_picture = "picture";
		public String CONN_pokes = "pokes";
		public String CONN_posts = "posts";
		public String CONN_questions = "questions";
		public String CONN_scores = "scores";
		public String CONN_sharedposts = "sharedposts";
		public String CONN_statuses = "statuses";
		public String CONN_subscribedto = "subscribedto";
		public String CONN_subscribers = "subscribers";
		public String CONN_tagged = "tagged";
		public String CONN_television = "television";
		public String CONN_updates = "updates";
		public String CONN_videos = "videos";
		//acounts
		private Achievement achievements;
		//activities;
		Album albums;
		//apprequest -> message:String data:String
		//Book
		Checkin checkins;
		Event events;
		//Family
		//feed
		FriendList friendlists;
		// FriendRequest
		Friends friends;
		//games
		Group groups;
		//Home
		//inbox
		//interest
		Like likes;
		//Links
		//location
		//Movies
		//Music
		//MutualFriends
		Note notes;
		//Notification
		//outbox
		//payments
		//permissions
		Photo photos;
		//Photos uploaded
		//picture/
		//pokes
		Post posts;
		Question questions;
		//scores
		//sharedposts
		//statuses
		ArrayList<From> subscribedto;
		ArrayList<From> subscrivers;
		//tagged
		//television
		//updates
		//videos
		
		public FbConnection(){
		}
	}
	/*
	private class Installed{
		private String type;
		private String id;
		private String installed;
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getInstalled() {
			return installed;
		}
		public void setInstalled(String installed) {
			this.installed = installed;
		}
		
	}*/
	private class Cover{
		private String id;
		private String source;
		private String offset_y;
		Cover(){
			
		}
		Cover(String id , String source, String offset_y){
			this.id = id;
			this.source = source;
			this.offset_y = offset_y;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getSource() {
			return source;
		}
		public void setSource(String source) {
			this.source = source;
		}
		public String getOffset_y() {
			return offset_y;
		}
		public void setOffset_y(String offset_y) {
			this.offset_y = offset_y;
		}
		
	}
	private class Devices{
		private String os;
		private String hardware;
		Devices(){
			
		}
		Devices(String os, String hardward){
			this.os = os;
			this.hardware = hardware;
		}
		public String getOs() {
			return os;
		}
		public void setOs(String os) {
			this.os = os;
		}
		public String getHardware() {
			return hardware;
		}
		public void setHardware(String hardware) {
			this.hardware = hardware;
		}
		
	}
	private class Education{
		private String year;
		private String type;
		private School school;
		
		private class School{
			private String name;
			private String id;
			private String type;
			private String year;
			private String degree;
			private ArrayList<String> concentration;
			private ArrayList<String> classes;
			private ArrayList<String> with;
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public String getId() {
				return id;
			}
			public void setId(String id) {
				this.id = id;
			}
			public String getType() {
				return type;
			}
			public void setType(String type) {
				this.type = type;
			}
			public String getYear() {
				return year;
			}
			public void setYear(String year) {
				this.year = year;
			}
			public String getDegree() {
				return degree;
			}
			public void setDegree(String degree) {
				this.degree = degree;
			}
			public ArrayList<String> getConcentration() {
				return concentration;
			}
			public void setConcentration(ArrayList<String> concentration) {
				this.concentration = concentration;
			}
			public ArrayList<String> getClasses() {
				return classes;
			}
			public void setClasses(ArrayList<String> classes) {
				this.classes = classes;
			}
			public ArrayList<String> getWith() {
				return with;
			}
			public void setWith(ArrayList<String> with) {
				this.with = with;
			}
		}
		public String getYear() {
			return year;
		}
		public void setYear(String year) {
			this.year = year;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public School getSchool() {
			return school;
		}
		public void setSchool(School school) {
			this.school = school;
		}
		
	}
	private class Work{
		private String employer;
		private String location;
		private String position;
		private String start_date;
		private String end_date;
		public String getEmployer() {
			return employer;
		}
		public void setEmployer(String employer) {
			this.employer = employer;
		}
		public String getLocation() {
			return location;
		}
		public void setLocation(String location) {
			this.location = location;
		}
		public String getPosition() {
			return position;
		}
		public void setPosition(String position) {
			this.position = position;
		}
		public String getStart_date() {
			return start_date;
		}
		public void setStart_date(String start_date) {
			this.start_date = start_date;
		}
		public String getEnd_date() {
			return end_date;
		}
		public void setEnd_date(String end_date) {
			this.end_date = end_date;
		}
		
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

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getMiddle_name() {
		return middle_name;
	}

	public void setMiddle_name(String middle_name) {
		this.middle_name = middle_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public ArrayList<From> getLanguages() {
		return languages;
	}

	public void setLanguages(ArrayList<From> languages) {
		this.languages = languages;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getThird_party_id() {
		return third_party_id;
	}

	public void setThird_party_id(String third_party_id) {
		this.third_party_id = third_party_id;
	}

	public int getTimezone() {
		return timezone;
	}

	public void setTimezone(int timezone) {
		this.timezone = timezone;
	}

	public String getUpdated_time() {
		return updated_time;
	}

	public void setUpdated_time(String updated_time) {
		this.updated_time = updated_time;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public ArrayList<Cover> getCover() {
		return cover;
	}

	public void setCover(ArrayList<Cover> cover) {
		this.cover = cover;
	}

	public ArrayList<Devices> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<Devices> devices) {
		this.devices = devices;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public From getHometown() {
		return hometown;
	}

	public void setHometown(From hometown) {
		this.hometown = hometown;
	}

	public ArrayList<String> getInterested_in() {
		return interested_in;
	}

	public void setInterested_in(ArrayList<String> interested_in) {
		this.interested_in = interested_in;
	}

	public From getLocation() {
		return location;
	}

	public void setLocation(From location) {
		this.location = location;
	}

	public String getPolitical() {
		return political;
	}

	public void setPolitical(String political) {
		this.political = political;
	}

	public ArrayList<From> getFavorite_athletes() {
		return favorite_athletes;
	}

	public void setFavorite_athletes(ArrayList<From> favorite_athletes) {
		this.favorite_athletes = favorite_athletes;
	}

	public ArrayList<From> getFavorite_team() {
		return favorite_team;
	}

	public void setFavorite_team(ArrayList<From> favorite_team) {
		this.favorite_team = favorite_team;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getQuotes() {
		return quotes;
	}

	public void setQuotes(String quotes) {
		this.quotes = quotes;
	}

	public String getRelationship_status() {
		return relationship_status;
	}

	public void setRelationship_status(String relationship_status) {
		this.relationship_status = relationship_status;
	}

	public String getReligion() {
		return religion;
	}

	public void setReligion(String religion) {
		this.religion = religion;
	}

	public String getSecurity_settings() {
		return security_settings;
	}

	public void setSecurity_settings(String security_settings) {
		this.security_settings = security_settings;
	}

	public From getSignificant_other() {
		return significant_other;
	}

	public void setSignificant_other(From significant_other) {
		this.significant_other = significant_other;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public ArrayList<Work> getWork() {
		return work;
	}

	public void setWork(ArrayList<Work> work) {
		this.work = work;
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
			name = json.getString("name");
			first_name = json.getString("first_name");
			middle_name = json.getString("middle_name");
			last_name = json.getString("last_name");
			gender = json.getString("gender");
			locale = json.getString("locale");
			
			languages = new ArrayList<From>();
			JSONObject languageObject = json.getJSONObject("languages");
			JSONArray languageArray = languageObject.getJSONArray("data");
			for(int i=0; i<languageArray.length(); i++){
				languages.add(new From(languageArray.getJSONObject(i).getString("id") , languageArray.getJSONObject(i).getString("name")));
			}
			
			link = json.getString("link");
			username = json.getString("username");
			//age_range
			
			third_party_id = json.getString("third_party_id");
			installed = json.getBoolean("installed");
			timezone = json.getInt("timezone");
			updated_time = json.getString("updated_time");
			verified = json.getBoolean("verfied");
			bio = json.getString("bio");
			birthday = json.getString("birthday");
		
			cover = new ArrayList<Cover>();
			JSONObject coverObject = json.getJSONObject("cover");
			JSONArray coverArray = json.getJSONArray("data");
			for(int i=0; i<coverArray.length() ; i++){
				cover.add(new Cover(coverArray.getJSONObject(i).getString("id"), coverArray.getJSONObject(i).getString("source") , coverArray.getJSONObject(i).getString("offset_y")));
			}
			
			devices = new ArrayList<Devices>();
			JSONObject deviceObject = json.getJSONObject("devices");
			JSONArray deviceArray = json.getJSONArray("data");
			for(int i=0; i<deviceArray.length() ; i++){
				devices.add(new Devices(deviceArray.getJSONObject(i).getString("os"), deviceArray.getJSONObject(i).getString("hardward")));
			}
			email = json.getString("email");
			
			JSONObject hometownObject = json.getJSONObject("hometown");
			hometown = new From(hometownObject.getString("id"), hometownObject.getString("name"));
			
			/*interested_in = new ArrayList<String>();
			JSONObject interestedObject = json.getJSONObject("interested_in");
			JSONArray interestedArray = interestedObject.getJSONArray("data");
			for(int i =0; i<interestedArray.length() ; i++){
				interested_in.add(interestedArray.getString(i));
			}*/ // need to confirm how it looks like in object
			
			JSONObject locationObject = json.getJSONObject("location");
			location = new From(locationObject.getString("id"), locationObject.getString("name"));
			
			political = json.getString("political");
			//paytment_pricepoints
			
			favorite_athletes = new ArrayList<From>();
			JSONObject athleteObject = json.getJSONObject("favorite_athletes");
			JSONArray athleteArray = athleteObject.getJSONArray("data");
			for(int i = 0 ; i< athleteArray.length(); i++){
				favorite_athletes.add( new From(athleteArray.getJSONObject(i).getString("id"), athleteArray.getJSONObject(i).getString("id")));
			}
			
			favorite_team = new ArrayList<From>();
			JSONObject teamObject = json.getJSONObject("favorite_team");
			JSONArray teamArray = athleteObject.getJSONArray("data");
			for(int i = 0 ; i< teamArray.length(); i++){
				favorite_team.add( new From(teamArray.getJSONObject(i).getString("id"), teamArray.getJSONObject(i).getString("id")));
			}
			picture = json.getString("picture");
			quotes = json.getString("quptes");
			relationship_status = json.getString("relationship_status");
			religion = json.getString("religion");
			//security setting
			JSONObject significantObject = json.getJSONObject("significant_other");
			significant_other = new From(significantObject.getString("id"), significantObject.getString("name"));
			website = json.getString("website");
			//work
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
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

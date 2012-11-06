package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;

import javax.security.auth.callback.LanguageCallback;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


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
	private Installed installed;
	private int timezone;
	private String updated_time;
	private boolean verified;
	private String bio;
	private String birthday;
	private ArrayList<Cover> cover;
	private ArrayList<Devices> devices;
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
		languages = new ArrayList<From>();
		cover = new ArrayList<Cover>();
		devices = new ArrayList<Devices>();
		interested_in = new ArrayList<String>();
		favorite_athletes = new ArrayList<From>();
		favorite_team = new ArrayList<From>();
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
		
		
		public FbConnection(){
		}
	}
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
		
	}
	private class Cover{
		private String id;
		private String source;
		private String offset_y;
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
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}

}

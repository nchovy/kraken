package org.krakenapps.socialweb.facebook.jsonobject;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;
import java.util.ArrayList;



public class Checkin implements FacebookGraphObject{

	private String id;
	private From from;
	private From tags;
	private Place place;
	private CheckinApplication application;
	private String createdTime;
	private Likes likes;
	private String message;
	private ArrayList<Comment> comments;
	private String type;
	private FbConnection fbConnection;
	public Checkin(){
		fbConnection = new FbConnection();
		from = new From();
		tags = new From();
		place = new Place();
		application = new CheckinApplication();
		likes = new Likes();
		comments = new ArrayList<Comment>(); // limited number object.
	}
	private class FbConnection{
		public String CONN_comments = "comments";
		public String CONN_likes = "likes";
		private ArrayList<Comment> comments;
		private Likes likes;
		public FbConnection(){
			comments = null;
			likes = null;
		}
		public ArrayList<Comment> getComments() {
			return comments;
		}
		public void setComments(ArrayList<Comment> comments) {
			this.comments = comments;
		}
		public Likes getLikes() {
			return likes;
		}
		public void setLikes(Likes likes) {
			this.likes = likes;
		}
		
	}
	
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public From getFrom() {
		return from;
	}

	public void setFrom(From from) {
		this.from = from;
	}

	public From getTags() {
		return tags;
	}

	public void setTags(From tags) {
		this.tags = tags;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	public CheckinApplication getApplication() {
		return application;
	}

	public void setApplication(CheckinApplication application) {
		this.application = application;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public Likes getLikes() {
		return likes;
	}

	public void setLikes(Likes likes) {
		this.likes = likes;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ArrayList<Comment> getComments() {
		return comments;
	}

	public void setComments(ArrayList<Comment> comments) {
		this.comments = comments;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public FbConnection getFbConnection() {
		return fbConnection;
	}

	public void setFbConnection(FbConnection fbConnection) {
		this.fbConnection = fbConnection;
	}
	
}

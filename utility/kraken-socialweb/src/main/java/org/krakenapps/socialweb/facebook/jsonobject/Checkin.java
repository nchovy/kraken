package org.krakenapps.socialweb.facebook.jsonobject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;

import java.util.ArrayList;
import java.util.Set;



public class Checkin implements FacebookGraphObject{

	private String id;
	private From from;
	private From tags;
	private Place place;
	private CheckinApplication application;
	private String createdTime;
	private Like likes;
	private String message;
	private ArrayList<Comment> comments;
	private String type;
	private FbConnection fbConnection;
	public Checkin(){
		fbConnection = new FbConnection();
	}
	private class FbConnection{
		public String CONN_comments = "comments";
		public String CONN_likes = "likes";
		private ArrayList<Comment> comments;
		private Like likes;
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
		public Like getLikes() {
			return likes;
		}
		public void setLikes(Like likes) {
			this.likes = likes;
		}
		
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

	public Like getLikes() {
		return likes;
	}

	public void setLikes(Like likes) {
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

	/* (non-Javadoc)
	 * @see org.krakenapps.socialweb.facebook.jsonobject.FacebookGraphObject#parseJson(org.json.JSONObject, java.util.Set)
	 */
	@Override
	public int parseJson(JSONObject json, Set<Permissions> permit) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int parseJson(JSONObject json) {
		try {
			id = json.getString("id");
		
			JSONObject fromObject = json.getJSONObject("from");
			from = new From(json.getString("id") , json.getString("name"));
			JSONObject tagsObject = json.getJSONObject("tags");
			tags = new From(json.getString("id") , json.getString("name"));
			
			place = new Place();
			JSONObject placeObject = json.getJSONObject("place");
			place = new Place(placeObject.getString("id"), placeObject.getString("name"), placeObject.getJSONObject("location").getInt("longitude"), placeObject.getJSONObject("location").getInt("latitude"));
			
			application = new CheckinApplication();
			JSONObject applicationObject = json.getJSONObject("application");
			application = new CheckinApplication(applicationObject.getString("id"), applicationObject.getString("canvasName"), applicationObject.getString("namespace"));
			
			createdTime = json.getString("created_time");
			
			JSONObject likeObject = json.getJSONObject("likes");
			JSONArray likeArray = likeObject.getJSONArray("data");
			ArrayList<From> likeList = new ArrayList<From>(likeObject.getInt("count"));
			for(int i=0; i<likeObject.getInt("count"); i++){
				likeList.add(new From(likeArray.getJSONObject(i).getString("id"),likeArray.getJSONObject(i).getString("name")));
			}
			likes = new Like(likeList , likeObject.getInt("count"));
			
			message = json.getString("message");
			
			comments = new ArrayList<Comment>(); // limited number object.
			JSONObject commentObject = json.getJSONObject("comments");
			JSONArray commentArray = commentObject.getJSONArray("data");
			for(int i =0 ; i<commentObject.getInt("count"); i++){
				Comment tmp = new Comment();
				tmp.setId(commentArray.getJSONObject(i).getString("id"));
				tmp.setFrom(new From(commentArray.getJSONObject(i).getJSONObject("from").getString("id"), commentArray.getJSONObject(i).getJSONObject("from").getString("name")));
				tmp.setMessage(commentArray.getJSONObject(i).getString("message"));
				tmp.setCreateTime(commentArray.getJSONObject(i).getString("created_time"));
				comments.add(tmp);
			}
			
			type = json.getString("type");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
}

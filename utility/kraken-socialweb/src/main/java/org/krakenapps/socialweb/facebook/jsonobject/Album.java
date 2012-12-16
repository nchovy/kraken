package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;


public class Album implements FacebookGraphObject{
	private String id;
	private From from;
	private String name;
	private String description;
	private String location; // it can be null
	private String link; // url
	private String coverPhotoID;
	private String privacy;
	private int count;
	private String type; //profile,mobile,wall,normal,album
	private String createdTime;
	private String updatedTime;
	private boolean canUpload;
	private FbConnection fbConnection;
	public Album(){
		fbConnection = new FbConnection();
	}
	private class FbConnection{
		public FbConnection(){
			photos = null;
			likes = null;
			comments = null;
		}
		private Photo photos;
		private Like likes;
		private ArrayList<Comment> comments ;
		private String pictures; // redirect url
		
		public String CONN_photos = "photos";
		public String CONN_likes = "likes";
		public String CONN_comments = "comments";
		public String CONN_pictures = "pictures";
		
		public Photo getPhotos() {
			return photos;
		}
		public void setPhotos(Photo photos) {
			this.photos = photos;
		}
		public Like getLikes() {
			return likes;
		}
		public void setLikes(Like likes) {
			this.likes = likes;
		}
		
		public ArrayList<Comment> getComments() {
			return comments;
		}
		public void setComments(ArrayList<Comment> comments) {
			this.comments = comments;
		}
		public String getPictures() {
			return pictures;
		}
		public void setPictures(String pictures) {
			this.pictures = pictures;
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
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getCoverPhotoID() {
		return coverPhotoID;
	}
	public void setCoverPhotoID(String coverPhotoID) {
		this.coverPhotoID = coverPhotoID;
	}
	public String getPrivacy() {
		return privacy;
	}
	public void setPrivacy(String privacy) {
		this.privacy = privacy;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}
	public String getUpdatedTime() {
		return updatedTime;
	}
	public void setUpdatedTime(String updatedTime) {
		this.updatedTime = updatedTime;
	}
	public boolean isCanUpload() {
		return canUpload;
	}
	public void setCanUpload(boolean canUpload) {
		this.canUpload = canUpload;
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
			from = new From(fromObject.getString("id"), fromObject.getString("name"));
			name = json.getString("name");
			description = json.getString("description");
			location = json.getString("location");
			link = json.getString("link");
			coverPhotoID = json.getString("cover_photo");
			privacy = json.getString("privacy");
			count = json.getInt("count");
			type = json.getString("type"); //profile,mobile,wall,normal,album
			createdTime = json.getString("created_time");
			updatedTime = json.getString("updated_time");
			canUpload = json.getBoolean("can_upload");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}
}

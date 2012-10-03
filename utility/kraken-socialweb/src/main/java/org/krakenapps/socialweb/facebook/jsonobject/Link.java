package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.Like;


public class Link implements FacebookGraphObject{

	private String id;
	private From from;
	private String link;
	private String name;
	private ArrayList<Comment> comments;
	private String description;
	private String icon;
	private String picture;
	private String message;
	private String created_time;
	private String type;
	private FbConnection fbConnection;
	private class FbConnection{
		public String CONN_comments = "comments";
		public String CONN_likes = "likes";
		private ArrayList<Comment> comments;
		private Like likes;
		public FbConnection(){
			comments =null;
			likes =null;
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
	public Link(){
		from = new From();
		fbConnection = new FbConnection();	
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
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Comment> getComments() {
		return comments;
	}
	public void setComments(ArrayList<Comment> comments) {
		this.comments = comments;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getCreated_time() {
		return created_time;
	}
	public void setCreated_time(String created_time) {
		this.created_time = created_time;
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

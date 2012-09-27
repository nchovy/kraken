package org.krakenapps.socialweb.facebook.jsonobject;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;


public class Album implements FacebookGraphObject{
	public Album(){
	}

	String id;
	From from;
	String name;
	String description;
	String location; // it can be null
	String link; // url
	String coverPhotoID;
	String privacy;
	int count;
	String type; //profile,mobile,wall,normal,album
	String createdTime;
	String updatedTime;
	boolean canUpload;
	Connection connection;
	class Connection{
		Photo photos;
		Likes likes;
		Comment comments;
		public String CONN_photos = "photos";
		public String CONN_likes = "likes";
		public String CONN_comments = "comments";
		/*
		 Album Class has Pictures Connection, but we can't Support Redirect function.
		*/
		public Photo getPhotos() {
			return photos;
		}
		public void setPhotos(Photo photos) {
			this.photos = photos;
		}
		public Likes getLikes() {
			return likes;
		}
		public void setLikes(Likes likes) {
			this.likes = likes;
		}
		public Comment getComments() {
			return comments;
		}
		public void setComments(Comment comments) {
			this.comments = comments;
		}
		
	}
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}
}

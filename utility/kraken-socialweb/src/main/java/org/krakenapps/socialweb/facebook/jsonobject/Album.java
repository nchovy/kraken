package org.krakenapps.socialweb.facebook.jsonobject;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;


public class Album implements FacebookGraphObject{
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
	FbConnection fbConnection;
	class FbConnection{
		Photo photos;
		Likes likes;
		Comment comments;
		String pictures; // redirect url
		
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
		public String getPictures() {
			return pictures;
		}
		public void setPictures(String pictures) {
			this.pictures = pictures;
		}
	}
	public Album(){
		from = new From();
		fbConnection = new FbConnection();
	}
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}

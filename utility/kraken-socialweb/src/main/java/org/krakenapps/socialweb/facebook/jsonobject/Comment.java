package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;

public class Comment implements FacebookGraphObject{

	private String id;
	private From from;
	private String message;
	private String createTime;
	private Likes likes;
	private String userLikes; // Always True;
	private String Type;
	private FbConnection fbConnection;

	public Comment(){
		from = new From();
		likes = new Likes();
		fbConnection = new FbConnection();
	}
	private class FbConnection{
		public String CONN_likes = "likes";
		private Likes likes;
		public FbConnection(){
			likes = null;
		}
		public Likes getLikes() {
			return likes;
		}
		public void setLikes(Likes likes) {
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public Likes getLikes() {
		return likes;
	}

	public void setLikes(Likes likes) {
		this.likes = likes;
	}

	public String getUserLikes() {
		return userLikes;
	}

	public void setUserLikes(String userLikes) {
		this.userLikes = userLikes;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}

	public FbConnection getFbConnection() {
		return fbConnection;
	}

	public void setFbConnection(FbConnection fbConnection) {
		this.fbConnection = fbConnection;
	}

}

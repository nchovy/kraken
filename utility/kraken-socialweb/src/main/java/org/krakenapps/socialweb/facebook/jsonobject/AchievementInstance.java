package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;



public class AchievementInstance implements FacebookGraphObject{
	private String id;
	private From from; // object containing the id and name of user
	private String createdTime; //
	private String application; // Application name
	private Achievement achievement;
	private Like likes;
	private ArrayList<Comment> comments;
	private FbConnection fbConnection;
	
	public AchievementInstance(){
		from = new From();
		likes = new Like();
		achievement = new Achievement();
		comments = new ArrayList<Comment>();
		fbConnection = new FbConnection();
	}

	
	private class FbConnection{
		// this Class has no connections
	};

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

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Achievement getAchievement() {
		return achievement;
	}

	public void setAchievement(Achievement achievement) {
		this.achievement = achievement;
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

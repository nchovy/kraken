package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;


/*if want to use this, you have to earn friends_games_activity permission*/
public class AchievementInstance implements FacebookGraphObject{
	private String id;
	private From from; // object containing the id and name of user
	private String createdTime; //
	private From application; // Application name
	private Achievement achievement;
	private Like likes;
	private ArrayList<Comment> comments;
	private FbConnection fbConnection;
	
	public AchievementInstance(){
		from = new From();
		application = new From();
		likes = new Like();
		achievement = new Achievement();
		comments = new ArrayList<Comment>();
		fbConnection = new FbConnection();
	}
	
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		try {
			id = json.getString("id");
			
			JSONObject fromObject = json.getJSONObject("from");
			this.from.setId(fromObject.getString("id"));
			this.from.setName(fromObject.getString("name"));
			createdTime = json.getString("created_time");
			
			JSONObject applicationObject = json.getJSONObject("application");
			application.setId(applicationObject.getString("id"));
			application.setName(applicationObject.getString("name"));
			
			JSONObject achiveObject = json.getJSONObject("achivement");
			achievement.setId(achiveObject.getString("id"));
			achievement.setUrl(achiveObject.getString("url"));
			achievement.setType(achiveObject.getString("type"));
			achievement.setTitle(achiveObject.getString("title"));
			
			JSONObject likeObject = json.getJSONObject("likes");
			JSONArray likeArray = likeObject.getJSONArray("data");
			likes.setCount(likeObject.getInt("count"));
			ArrayList<From> likeList = new ArrayList<From>(likes.getCount());
			for(int i=0; i<likes.getCount(); i++){
				likeList.add(new From(likeArray.getJSONObject(i).getString("id"),likeArray.getJSONObject(i).getString("name")));
			}
			likes.setLikeList(likeList);
			
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
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
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

	public From getApplication() {
		return application;
	}

	public void setApplication(From application) {
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


	public FbConnection getFbConnection() {
		return fbConnection;
	}

	public void setFbConnection(FbConnection fbConnection) {
		this.fbConnection = fbConnection;
	}

}

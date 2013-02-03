package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
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
		fbConnection = new FbConnection();
	}
	
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		try {
			id = json.getString("id");
			
			JSONObject fromObject = json.getJSONObject("from");
			from = new From(fromObject.getString("id"),fromObject.getString("name"));

			createdTime = json.getString("created_time");
			
			JSONObject applicationObject = json.getJSONObject("application");
			application = new From(applicationObject.getString("id"),applicationObject.getString("name"));
			
			achievement = new Achievement();
			JSONObject achiveObject = json.getJSONObject("achivement");
			achievement.setId(achiveObject.getString("id"));
			achievement.setUrl(achiveObject.getString("url"));
			achievement.setType(achiveObject.getString("type"));
			achievement.setTitle(achiveObject.getString("title"));
			
			likes = new Like();
			JSONObject likeObject = json.getJSONObject("likes");
			JSONArray likeArray = likeObject.getJSONArray("data");
			ArrayList<From> likeList = new ArrayList<From>(likeObject.getInt("count"));
			for(int i=0; i<likeObject.getInt("count"); i++){
				likeList.add(new From(likeArray.getJSONObject(i).getString("id"),likeArray.getJSONObject(i).getString("name")));
			}
			likes = new Like(likeList , likeObject.getInt("count"));
			
			comments = new ArrayList<Comment>();
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

	/* (non-Javadoc)
	 * @see org.krakenapps.socialweb.facebook.jsonobject.FacebookGraphObject#parseJson(org.json.JSONObject, java.util.Set)
	 */
	@Override
	public int parseJson(JSONObject json, Set<Permissions> permit) {
		// TODO Auto-generated method stub
		return 0;
	}

}

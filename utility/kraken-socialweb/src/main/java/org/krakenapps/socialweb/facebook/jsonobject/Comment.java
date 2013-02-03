package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;

public class Comment implements FacebookGraphObject{

	private String id;
	private From from;
	private String message;
	private String createTime;
	private Like likes;
	private String userLikes; // Always True;
	private String type;
	private FbConnection fbConnection;

	public Comment(){
		fbConnection = new FbConnection();
	}
	private class FbConnection{
		public String CONN_likes = "likes";
		private Like likes;
		public FbConnection(){
			likes = null;
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

	public Like getLikes() {
		return likes;
	}

	public void setLikes(Like likes) {
		this.likes = likes;
	}

	public String getUserLikes() {
		return userLikes;
	}

	public void setUserLikes(String userLikes) {
		this.userLikes = userLikes;
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
			message = json.getString("message");
			createTime = json.getString("created_time");
	
			likes = new Like();
			JSONObject likeObject = json.getJSONObject("likes");
			JSONArray likeArray = likeObject.getJSONArray("data");
			ArrayList<From> likeList = new ArrayList<From>(likeObject.getInt("count"));
			for(int i=0; i<likeObject.getInt("count"); i++){
				likeList.add(new From(likeArray.getJSONObject(i).getString("id"),likeArray.getJSONObject(i).getString("name")));
			}
			likes = new Like(likeList , likeObject.getInt("count"));
			
			userLikes = json.getString("user_likes");
			type = json.getString("type");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

}

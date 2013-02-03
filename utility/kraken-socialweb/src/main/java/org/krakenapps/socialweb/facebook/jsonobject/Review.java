package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class Review implements FacebookGraphObject{

	private String id;
	private From from;
	private From to;
	private String message;
	private int rating;
	private String created_time;
	private FbConnection fbConnection;
	
	private class FbConnection{
		public FbConnection(){
		}
	}
	public Review(){
		fbConnection = new FbConnection();
		message = null;
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
	public From getTo() {
		return to;
	}
	public void setTo(From to) {
		this.to = to;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public String getCreated_time() {
		return created_time;
	}
	public void setCreated_time(String created_time) {
		this.created_time = created_time;
	}
	public FbConnection getFbConnection() {
		return fbConnection;
	}
	public void setFbConnection(FbConnection fbConnection) {
		this.fbConnection = fbConnection;
	}
	@Override
	public int parseJson(JSONObject json) {
		try {
			id = json.getString("id");
			
			JSONObject fromObject = json.getJSONObject("from");
			from = new From(fromObject.getString("id"), fromObject.getString("name"));
			JSONObject toObject = json.getJSONObject("to");
			from = new From(toObject.getString("id"), toObject.getString("name"));
			
			if(json.containsKey("message")){
				message = json.getString("message");
			} // because this section is optional field
			
			rating = json.getInt("rating");
			created_time = json.getString("created_time");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
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

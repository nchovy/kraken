package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class Message implements FacebookGraphObject{

	private String id;
	private String created_time;
	private From from;
	private ArrayList<From> to;
	private String message;
	private FbConnection fbConnection;
	private class FbConnection{
		public FbConnection(){
			
		}
		// there is no Connection
	}
	public Message(){
		fbConnection = new FbConnection();
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreated_time() {
		return created_time;
	}

	public void setCreated_time(String created_time) {
		this.created_time = created_time;
	}

	public From getFrom() {
		return from;
	}

	public void setFrom(From from) {
		this.from = from;
	}

	public ArrayList<From> getTo() {
		return to;
	}

	public void setTo(ArrayList<From> to) {
		this.to = to;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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
			created_time = json.getString("created_time");
			JSONObject fromObject = json.getJSONObject("from");
			from = new From(fromObject.getString("id"),fromObject.getString("name"));
			
			to = new ArrayList<From>();
			JSONObject toObject = json.getJSONObject("to");
			JSONArray toArray = toObject.getJSONArray("data");
			for(int i =0; i<toArray.length() ; i++){
				to.add( new From(toArray.getJSONObject(i).getString("id"),toArray.getJSONObject(i).getString("name")) );
			}
			message = json.getString("message");
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

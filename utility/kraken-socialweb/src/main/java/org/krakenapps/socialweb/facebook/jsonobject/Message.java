package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;

import org.json.JSONObject;
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
		from = new From();
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
		// TODO Auto-generated method stub
		return 0;
	}

}

package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class Thread implements FacebookGraphObject{

	private String id;
	private String snippet;
	private String updated_time;
	private int message_count;
	private int unread_count;
	private ArrayList<From> tags;
	// participants;
	//fromer_participants;
	//senders
	private ArrayList<Message> messages;
	private FbConnection fbConnection;
	
	private class FbConnection{
		
		//tags
		//participants
		//senders
		ArrayList<Message> messages;
		public FbConnection(){
		}
	}
	public Thread(){
		tags = new ArrayList<From>();
		messages = new ArrayList<Message>();
		fbConnection = new FbConnection();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public String getUpdated_time() {
		return updated_time;
	}

	public void setUpdated_time(String updated_time) {
		this.updated_time = updated_time;
	}

	public int getMessage_count() {
		return message_count;
	}

	public void setMessage_count(int message_count) {
		this.message_count = message_count;
	}

	public int getUnread_count() {
		return unread_count;
	}

	public void setUnread_count(int unread_count) {
		this.unread_count = unread_count;
	}

	public ArrayList<From> getTags() {
		return tags;
	}

	public void setTags(ArrayList<From> tags) {
		this.tags = tags;
	}

	public ArrayList<Message> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<Message> messages) {
		this.messages = messages;
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

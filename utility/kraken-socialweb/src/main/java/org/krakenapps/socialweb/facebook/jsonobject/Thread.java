package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class Thread implements FacebookGraphObject{

	private String id;
	private String snippet;
	private String updated_time;
	private int message_count;
	private int unread_count;
	private ArrayList<String> tags;
	private ArrayList<participant> participants;
	private ArrayList<participant> former_participants;
	private ArrayList<participant> senders;
	private ArrayList<Message> messages;
	private FbConnection fbConnection;
	
	private class FbConnection{
		public String CONN_tags = "tags";
		public String CONN_participants = "participants";
		public String CONN_former_participants = "former_participants";
		public String CONN_senders = "senders";
		public String CONN_messages = "messages";
		private ArrayList<Message> messages;
		private ArrayList<String> tags;
		private ArrayList<participant> participants;
		private ArrayList<participant> former_participants;
		private ArrayList<participant> senders;
		public FbConnection(){
			messages = null;
			participants = null;
			former_participants =null;
			senders = null;
		}
		public ArrayList<Message> getMessages() {
			return messages;
		}
		public void setMessages(ArrayList<Message> messages) {
			this.messages = messages;
		}
		public ArrayList<String> getTags() {
			return tags;
		}
		public void setTags(ArrayList<String> tags) {
			this.tags = tags;
		}
		public ArrayList<participant> getParticipants() {
			return participants;
		}
		public void setParticipants(ArrayList<participant> participants) {
			this.participants = participants;
		}
		public ArrayList<participant> getFormer_participants() {
			return former_participants;
		}
		public void setFormer_participants(ArrayList<participant> former_participants) {
			this.former_participants = former_participants;
		}
		public ArrayList<participant> getSenders() {
			return senders;
		}
		public void setSenders(ArrayList<participant> senders) {
			this.senders = senders;
		}
		
	}
	private class participant{
		private String id;
		private String email;
		private String name;
		participant(){
			
		}
		participant(String id , String email, String name){
			this.id = id;
			this.email = email;
			this.name = name;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
	public Thread(){
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
		try {
			id = json.getString("id");
			snippet = json.getString("snippet");
			updated_time = json.getString("updated_time");
			message_count = json.getInt("message_count");
			unread_count = json.getInt("unread_count");
			
			tags = new ArrayList<String>();
			JSONObject tagObject = json.getJSONObject("tags");
			JSONArray tagArray = tagObject.getJSONArray("data");
			for(int i = 0 ; i<tagArray.length() ; i++){
				tags.add(tagArray.getJSONObject(i).getString("name"));
			}
			
			participants = new ArrayList<Thread.participant>();
			JSONObject participantsObject = json.getJSONObject("participants");
			JSONArray participantsArray = participantsObject.getJSONArray("data");
			for(int i =0; i< participantsArray.length(); i++){
				participants.add(new participant(participantsArray.getJSONObject(i).getString("id"), participantsArray.getJSONObject(i).getString("email") , participantsArray.getJSONObject(i).getString("name")));
			}
			
			former_participants = new ArrayList<Thread.participant>();
			JSONObject formerParticipantsObject = json.getJSONObject("former_participants");
			JSONArray formerParticipantsArray = formerParticipantsObject.getJSONArray("data");
			for(int i =0; i< formerParticipantsArray.length(); i++){
				former_participants.add(new participant(formerParticipantsArray.getJSONObject(i).getString("id"), formerParticipantsArray.getJSONObject(i).getString("email") , formerParticipantsArray.getJSONObject(i).getString("name")));
			}
			
			senders = new ArrayList<Thread.participant>();
			JSONObject sendersObject = json.getJSONObject("senders");
			JSONArray sendersArray = sendersObject.getJSONArray("data");
			for(int i =0; i< sendersArray.length(); i++){
				senders.add(new participant(sendersArray.getJSONObject(i).getString("id"), sendersArray.getJSONObject(i).getString("email") , sendersArray.getJSONObject(i).getString("name")));
			}
			
			messages = new ArrayList<Message>();
			JSONObject messagesObject = json.getJSONObject("messages");
			JSONArray messagesArray = messagesObject.getJSONArray("data");
			for(int i =0 ; i< messagesArray.length() ; i++){
				Message tmp = new Message();
				tmp.parseJson(messagesArray.getJSONObject(i));
				messages.add(tmp);
			}
			
			
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

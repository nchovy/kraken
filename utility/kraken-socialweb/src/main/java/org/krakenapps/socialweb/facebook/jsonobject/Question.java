package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class Question implements FacebookGraphObject{

	private String id;
	private From from;
	private String question;
	private String created_time;
	private String updated_time;
	private FbConnection fbConnection;
	private ArrayList<QuestionOption> options;
	private class FbConnection{
		public String CONN_options = "options";
		private ArrayList<QuestionOption> options;
		public FbConnection(){
			options = null;
		}
		public ArrayList<QuestionOption> getOptions() {
			return options;
		}
		public void setOptions(ArrayList<QuestionOption> options) {
			this.options = options;
		}
	}
	
	public Question(){
		fbConnection = new FbConnection();
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

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getCreated_time() {
		return created_time;
	}

	public void setCreated_time(String created_time) {
		this.created_time = created_time;
	}

	public String getUpdated_time() {
		return updated_time;
	}

	public void setUpdated_time(String updated_time) {
		this.updated_time = updated_time;
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
			question = json.getString("question");
			created_time = json.getString("created_time");
			updated_time = json.getString("updated_time");
			JSONObject optionsObject = json.getJSONObject("options");
			JSONArray optionsArray = optionsObject.getJSONArray("data");
			for(int i = 0 ; i< optionsArray.length(); i++){
				QuestionOption tmp = new QuestionOption();
				tmp.parseJson(optionsArray.getJSONObject(i));
				options.add(tmp);
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

package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class QuestionOption implements FacebookGraphObject{
	private String id;
	private String name;
	private From from;
	private int vote_count;
	private Category object;
	private String created_time;
	private FbConnection fbConnection;
	private class FbConnection{
		public String CONN_votes = "votes";
		private ArrayList<From> votes;
		public FbConnection(){
			votes = null;
		}
		public ArrayList<From> getVotes() {
			return votes;
		}

		public void setVotes(ArrayList<From> votes) {
			this.votes = votes;
		}
		
	}
	private class Category{
		private String id;
		private String name;
		private String category;
		public Category(){
			
		}
		public Category(String id, String name, String category){
			this.id = id;
			this.name = name;
			this.category = category;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}
		public String getCreated_time() {
			return created_time;
		}
		
	}
	public QuestionOption(){
		fbConnection = new FbConnection();
	}
	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public From getFrom() {
		return from;
	}


	public void setFrom(From from) {
		this.from = from;
	}


	public Category getObject() {
		return object;
	}


	public void setObject(Category object) {
		this.object = object;
	}


	public String getCreated_time() {
		return created_time;
	}


	public void setCreated_time(String created_time) {
		this.created_time = created_time;
	}


	@Override
	public int parseJson(JSONObject json) {
		try {
			id = json.getString("id");
			
			JSONObject fromObject = json.getJSONObject("from");
			from = new From(fromObject.getString("id"), fromObject.getString("name"));
			
			name = json.getString("name");
			vote_count = json.getInt("vote_count");
			
			JSONObject categoryObject = json.getJSONObject("object");
			object = new Category(categoryObject.getString("id"), categoryObject.getString("name"), categoryObject.getString("category"));
			
			created_time = json.getString("created_time");
		} catch (JSONException e) {
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

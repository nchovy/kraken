package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class QuestionOption implements FacebookGraphObject{
	private String id;
	private String name;
	private From from;
	private int votes;
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
		private String created_time;
		public Category(){
			
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
		public void setCreated_time(String created_time) {
			this.created_time = created_time;
		}
		
	}
	public QuestionOption(){
		from = new From();
		object = new Category();
		votes = -1;
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


	public int getVotes() {
		return votes;
	}


	public void setVotes(int votes) {
		this.votes = votes;
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
		// TODO Auto-generated method stub
		return 0;
	}

}

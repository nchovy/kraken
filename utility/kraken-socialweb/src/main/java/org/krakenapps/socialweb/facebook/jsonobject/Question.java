package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class Question implements FacebookGraphObject{

	private String id;
	private From from;
	private String question;
	private String created_time;
	private String updated_time;
	private FbConnection fbConnection;
	private ArrayList<String> options;
	private class FbConnection{
		public String CONN_options = "options";
		private ArrayList<QuestionOption> options;
		private class QuestionOption{
			private String id;
			private From from;
			private String name;
			private int vote;
			private Category object;
			private String created_time;
			private class InnerConnection{
				public String CONN_votes = "votes";
				private ArrayList<From> votes;
				public InnerConnection(){
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
				from = null;
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
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public int getVote() {
				return vote;
			}
			public void setVote(int vote) {
				this.vote = vote;
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
			
		}
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
		from = new From();
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

	public ArrayList<String> getOptions() {
		return options;
	}

	public void setOptions(ArrayList<String> options) {
		this.options = options;
	}

	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}

}

package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;

import org.json.JSONObject;


public class Insight implements FacebookGraphObject{

	private String id;
	private String name;
	private String period;
	private ArrayList<Values> values;
	private String Description;
	private FbConnection fbConnection;

	private class FbConnection{
		public FbConnection(){
			
		}
	}
	private class Values{
		String val;
		String end_time;
		public String getVal() {
			return val;
		}
		public void setVal(String val) {
			this.val = val;
		}
		public String getEnd_time() {
			return end_time;
		}
		public void setEnd_time(String end_time) {
			this.end_time = end_time;
		}
		
	}
	public Insight(){
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
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public ArrayList<Values> getValues() {
		return values;
	}
	public void setValues(ArrayList<Values> values) {
		this.values = values;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
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

package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class Insight implements FacebookGraphObject{

	private String id;
	private String name;
	private String period;
	private ArrayList<Values> values;
	private String description;
	private FbConnection fbConnection;

	private class FbConnection{
		public FbConnection(){
			
		}
	}
	private class Values{
		String val;
		String end_time;
		Values(String val, String end_time){
			this.val = val;
			this.end_time = end_time;
		}
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
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	/* (non-Javadoc)
	 * @see org.krakenapps.socialweb.facebook.jsonobject.FacebookGraphObject#parseJson(org.json.JSONObject, java.util.Set)
	 */
	@Override
	public int parseJson(JSONObject json, Set<Permissions> permit) {
		try {
			id = json.getString("id");
			name = json.getString("name");
			period = json.getString("period");
			
			values = new ArrayList<Insight.Values>();
			JSONObject valueObject = json.getJSONObject("values");
			JSONArray valueArray = valueObject.getJSONArray("data");
			for(int i =0 ; i<valueArray.length(); i++){
				values.add(new Values(valueArray.getJSONObject(i).getString("value"), valueArray.getJSONObject(i).getString("end_time")));
			}
			
			description = json.getString("description");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

}

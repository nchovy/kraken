package org.krakenapps.socialweb.facebook.jsonobject;

import org.json.JSONObject;

public class Domain implements FacebookGraphObject{

	private String id;
	private String name;
	private FbConnection fbConnection;
	
	public Domain(){
		fbConnection = new FbConnection();
	}
	private class FbConnection{
		public FbConnection(){
			
		}
		// connection is null
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

	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}

	public FbConnection getFbConnection() {
		return fbConnection;
	}

	public void setFbConnection(FbConnection fbConnection) {
		this.fbConnection = fbConnection;
	}
	
}

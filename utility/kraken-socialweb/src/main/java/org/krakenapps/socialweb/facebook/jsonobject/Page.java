package org.krakenapps.socialweb.facebook.jsonobject;

import org.json.JSONObject;

public class Page implements FacebookGraphObject{

	String id;
	String name;
	String link;
	String category;
	boolean is_published;
	boolean can_post;
	int likes;
	String location;
	String phone;
	int checkins;
	String pictures;
	String cover;
	String website;
	int talking_about_count;
	String access_token;
	
	private class FbConnection{
		
	}
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}

}

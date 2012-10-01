package org.krakenapps.socialweb.facebook.jsonobject;

import org.json.JSONObject;

public class Page implements FacebookGraphObject{

	private String id;
	private String name;
	private String link;
	private String category;
	private boolean is_published;
	private boolean can_post;
	private int likes;
	private String location;
	private String phone;
	private int checkins;
	private String pictures;
	private String cover;
	private String website;
	private int talking_about_count;
	private String access_token;
	
	private class FbConnection{
		
	}
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}

}

package org.krakenapps.socialweb.facebook.jsonobject;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;
import java.util.ArrayList;



public class Checkin implements FacebookGraphObject{

	String id;
	From from;
	From tags;
	Place place;
	CheckinApplication application;
	String createdTime;
	Likes likes;
	String message;
	ArrayList<Comment> comments;
	String type;
	public class Connections{
		ArrayList<Comment> comments;
		Likes likes;
	}
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}
}

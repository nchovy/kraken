package org.krakenapps.socialweb.facebook.jsonobject;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;
import java.util.ArrayList;



public class Checkin implements FacebookGraphObject{

	public Checkin(){
		fbConnection = new FbConnection();
		from = new From();
		tags = new From();
		place = new Place();
		application = new CheckinApplication();
		likes = new Likes();
		comments = new ArrayList<Comment>(); // limited number object.
	}
	
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
	FbConnection fbConnection;
	public class FbConnection{
		ArrayList<Comment> comments;
		Likes likes;
		public FbConnection(){
			comments = new ArrayList<Comment>();
			likes = new Likes();
		}
		
		
		
	}
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}
}

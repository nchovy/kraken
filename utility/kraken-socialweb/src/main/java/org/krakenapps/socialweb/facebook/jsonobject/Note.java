package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.Likes;


public class Note implements FacebookGraphObject{

	private String id;
	private From from;
	private String subject;
	private String message;
	private ArrayList<Comment> comments;
	private String created_time;
	private String updated_time;
	private String icon;
	
	private class FbConnection{
		public String CONN_comments = "comments";
		public String CONN_likes = "likes";
		private ArrayList<Comment> comments;
		private Likes likes;
		public FbConnection(){
			comments =null;
			likes =null;
		}
	}
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		
		return 0;
	}

}

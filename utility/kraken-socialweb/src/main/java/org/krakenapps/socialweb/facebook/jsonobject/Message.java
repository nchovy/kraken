package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class Message implements FacebookGraphObject{

	private String id;
	private String created_time;
	private From from;
	private ArrayList<From> to;
	private String message;
	private FbConnection fbConnection;
	public Message(){
		from = new From();
		fbConnection = new FbConnection();
	}
	private class FbConnection{
		public FbConnection(){
			
		}
		// there is no Connection
	}
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}

}

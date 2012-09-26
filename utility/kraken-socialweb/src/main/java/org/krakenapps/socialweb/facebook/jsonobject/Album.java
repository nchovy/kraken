package org.krakenapps.socialweb.facebook.jsonobject;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;


public class Album implements FacebookGraphObject{

	String id;
	From from;
	String name;
	String description;
	String location; // it can be null
	String link; // url
	String coverPhotoID;
	String privacy;
	int count;
	String type; //profile,mobile,wall,normal,album
	String createdTime;
	String updatedTime;
	boolean canUpload;
	private class Connection{
		Photo photos;
		
	}
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}
}

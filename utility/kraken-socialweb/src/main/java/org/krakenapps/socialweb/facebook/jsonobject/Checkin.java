package org.krakenapps.socialweb.facebook.jsonobject;

import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;
import java.util.ArrayList;



public class Checkin {

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
}

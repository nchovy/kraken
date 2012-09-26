package org.krakenapps.socialweb.facebook.jsonobject;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;


public class Application implements FacebookGraphObject{

	String id;
	String name;
	String description;
	String category;
	String company;
	String iconUrl;
	String subcategory;
	String link;
	String logoUrl;
	int dailyActiveUsers;
	int weeklyActiveUsers;
	int monthlyActiveUsers;
	// Migration?
	String namespace;
	Restrictions restrictions;
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}

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
	String daily_active_users;
	String weekly_active_users;
	String monthly_active_users;
	// facebook return object we defined above
	// TODO : Migration?
	String namespace;
	Restrictions restrictions; //Object with one or more of the following fields: type, location, age, and age_distr
	// TODO : app_domains?
	String auth_dialog_data_help_url;
	String auth_dialog_description;
	String auth_dialog_headline;
	String auth_dialog_perms_explanation;
	/* Array types?
	auth_referral_user_perms
	auth_referral_friend_perms
	*/
	String auth_referral_default_activity_privacy; // SELF , EVERYONE , ALL_FRIENDS or NONE
	boolean auth_referral_enabled;
	/* Array
	auth_referral_enabled
	*/
	String auth_referral_response_type; // code or token
	boolean canvas_fluid_height;
	boolean canvas_fluid_width;
	String canvas_url;
	String contact_email;
	int created_time;
	int creator_uid;
	String deauth_callback_url;
	String iphone_app_store_id;
	String hosting_url;
	String mobile_web_url;
	String page_tab_default_name;
	String page_tab_url;
	String privacy_policy_url;
	String secure_canvas_url;
	String secure_page_tab_url;
	String server_ip_whitelist;
	boolean social_discovery;
	String terms_of_service_url;
	String user_support_email;
	String user_support_url;
	String website_url;
	FbConnection fbConnection;
	
	public Application(){
		fbConnection = new FbConnection();
	}
	public class FbConnection{
		public String CONN_accounts = "accounts";
		public String CONN_achievements = "achievements";
		public String CONN_banned = "banned";
		public String CONN_insights = "insights";
		public String CONN_payment_currencies = "payment_currencies";
		public String CONN_payment = "payment";
		public String CONN_picture = "picture";
		public String CONN_roles = "roles";
		public String CONN_staticresources = "staticresources";
		public String CONN_subscriptions = "subscriptions";
		public String CONN_translations = "translations";
		public String CONN_scores = "scores";
		public String CONN_video = "video";
		// TODO: define field Element
		
	}
	
	
	
	@Override
	public int parseJson(JSONObject json) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}

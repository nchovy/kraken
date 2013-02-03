package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.Set;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;

public class Application implements FacebookGraphObject {
	private String id;
	private String name;
	private String description;
	private String category;
	private String company;
	private String iconUrl;
	private String subcategory;
	private String link;
	private String logoUrl;
	private String daily_active_users;
	private String weekly_active_users;
	private String monthly_active_users;
	// facebook return object we defined above
	// TODO : Migration?
	private String namespace;
	private Restrictions restrictions; // Object with one or more of the following
								// fields: type, location, age, and age_distr
	// TODO : app_domains?
	private String auth_dialog_data_help_url;
	private String auth_dialog_description;
	private String auth_dialog_headline;
	private String auth_dialog_perms_explanation;
	/*
	 * Array types? auth_referral_user_perms auth_referral_friend_perms
	 */
	private String auth_referral_default_activity_privacy; // SELF , EVERYONE ,
													// ALL_FRIENDS or NONE
	private boolean auth_referral_enabled;
	/*
	 * Array auth_referral_enabled
	 */
	private String auth_referral_response_type; // code or token
	private boolean canvas_fluid_height;
	private boolean canvas_fluid_width;
	private String canvas_url;
	private String contact_email;
	private int created_time;
	private int creator_uid;
	private String deauth_callback_url;
	private String iphone_app_store_id;
	private String hosting_url;
	private String mobile_web_url;
	private String page_tab_default_name;
	private String page_tab_url;
	private String privacy_policy_url;
	private String secure_canvas_url;
	private String secure_page_tab_url;
	private String server_ip_whitelist;
	private boolean social_discovery;
	private String terms_of_service_url;
	private String user_support_email;
	private String user_support_url;
	private String website_url;
	private FbConnection fbConnection;

	public Application() {
		fbConnection = new FbConnection();
	}

	private class FbConnection {
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getSubcategory() {
		return subcategory;
	}

	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getDaily_active_users() {
		return daily_active_users;
	}

	public void setDaily_active_users(String daily_active_users) {
		this.daily_active_users = daily_active_users;
	}

	public String getWeekly_active_users() {
		return weekly_active_users;
	}

	public void setWeekly_active_users(String weekly_active_users) {
		this.weekly_active_users = weekly_active_users;
	}

	public String getMonthly_active_users() {
		return monthly_active_users;
	}

	public void setMonthly_active_users(String monthly_active_users) {
		this.monthly_active_users = monthly_active_users;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public Restrictions getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(Restrictions restrictions) {
		this.restrictions = restrictions;
	}

	public String getAuth_dialog_data_help_url() {
		return auth_dialog_data_help_url;
	}

	public void setAuth_dialog_data_help_url(String auth_dialog_data_help_url) {
		this.auth_dialog_data_help_url = auth_dialog_data_help_url;
	}

	public String getAuth_dialog_description() {
		return auth_dialog_description;
	}

	public void setAuth_dialog_description(String auth_dialog_description) {
		this.auth_dialog_description = auth_dialog_description;
	}

	public String getAuth_dialog_headline() {
		return auth_dialog_headline;
	}

	public void setAuth_dialog_headline(String auth_dialog_headline) {
		this.auth_dialog_headline = auth_dialog_headline;
	}

	public String getAuth_dialog_perms_explanation() {
		return auth_dialog_perms_explanation;
	}

	public void setAuth_dialog_perms_explanation(
			String auth_dialog_perms_explanation) {
		this.auth_dialog_perms_explanation = auth_dialog_perms_explanation;
	}

	public String getAuth_referral_default_activity_privacy() {
		return auth_referral_default_activity_privacy;
	}

	public void setAuth_referral_default_activity_privacy(
			String auth_referral_default_activity_privacy) {
		this.auth_referral_default_activity_privacy = auth_referral_default_activity_privacy;
	}

	public boolean isAuth_referral_enabled() {
		return auth_referral_enabled;
	}

	public void setAuth_referral_enabled(boolean auth_referral_enabled) {
		this.auth_referral_enabled = auth_referral_enabled;
	}

	public String getAuth_referral_response_type() {
		return auth_referral_response_type;
	}

	public void setAuth_referral_response_type(
			String auth_referral_response_type) {
		this.auth_referral_response_type = auth_referral_response_type;
	}

	public boolean isCanvas_fluid_height() {
		return canvas_fluid_height;
	}

	public void setCanvas_fluid_height(boolean canvas_fluid_height) {
		this.canvas_fluid_height = canvas_fluid_height;
	}

	public boolean isCanvas_fluid_width() {
		return canvas_fluid_width;
	}

	public void setCanvas_fluid_width(boolean canvas_fluid_width) {
		this.canvas_fluid_width = canvas_fluid_width;
	}

	public String getCanvas_url() {
		return canvas_url;
	}

	public void setCanvas_url(String canvas_url) {
		this.canvas_url = canvas_url;
	}

	public String getContact_email() {
		return contact_email;
	}

	public void setContact_email(String contact_email) {
		this.contact_email = contact_email;
	}

	public int getCreated_time() {
		return created_time;
	}

	public void setCreated_time(int created_time) {
		this.created_time = created_time;
	}

	public int getCreator_uid() {
		return creator_uid;
	}

	public void setCreator_uid(int creator_uid) {
		this.creator_uid = creator_uid;
	}

	public String getDeauth_callback_url() {
		return deauth_callback_url;
	}

	public void setDeauth_callback_url(String deauth_callback_url) {
		this.deauth_callback_url = deauth_callback_url;
	}

	public String getIphone_app_store_id() {
		return iphone_app_store_id;
	}

	public void setIphone_app_store_id(String iphone_app_store_id) {
		this.iphone_app_store_id = iphone_app_store_id;
	}

	public String getHosting_url() {
		return hosting_url;
	}

	public void setHosting_url(String hosting_url) {
		this.hosting_url = hosting_url;
	}

	public String getMobile_web_url() {
		return mobile_web_url;
	}

	public void setMobile_web_url(String mobile_web_url) {
		this.mobile_web_url = mobile_web_url;
	}

	public String getPage_tab_default_name() {
		return page_tab_default_name;
	}

	public void setPage_tab_default_name(String page_tab_default_name) {
		this.page_tab_default_name = page_tab_default_name;
	}

	public String getPage_tab_url() {
		return page_tab_url;
	}

	public void setPage_tab_url(String page_tab_url) {
		this.page_tab_url = page_tab_url;
	}

	public String getPrivacy_policy_url() {
		return privacy_policy_url;
	}

	public void setPrivacy_policy_url(String privacy_policy_url) {
		this.privacy_policy_url = privacy_policy_url;
	}

	public String getSecure_canvas_url() {
		return secure_canvas_url;
	}

	public void setSecure_canvas_url(String secure_canvas_url) {
		this.secure_canvas_url = secure_canvas_url;
	}

	public String getSecure_page_tab_url() {
		return secure_page_tab_url;
	}

	public void setSecure_page_tab_url(String secure_page_tab_url) {
		this.secure_page_tab_url = secure_page_tab_url;
	}

	public String getServer_ip_whitelist() {
		return server_ip_whitelist;
	}

	public void setServer_ip_whitelist(String server_ip_whitelist) {
		this.server_ip_whitelist = server_ip_whitelist;
	}

	public boolean isSocial_discovery() {
		return social_discovery;
	}

	public void setSocial_discovery(boolean social_discovery) {
		this.social_discovery = social_discovery;
	}

	public String getTerms_of_service_url() {
		return terms_of_service_url;
	}

	public void setTerms_of_service_url(String terms_of_service_url) {
		this.terms_of_service_url = terms_of_service_url;
	}

	public String getUser_support_email() {
		return user_support_email;
	}

	public void setUser_support_email(String user_support_email) {
		this.user_support_email = user_support_email;
	}

	public String getUser_support_url() {
		return user_support_url;
	}

	public void setUser_support_url(String user_support_url) {
		this.user_support_url = user_support_url;
	}

	public String getWebsite_url() {
		return website_url;
	}

	public void setWebsite_url(String website_url) {
		this.website_url = website_url;
	}

	public FbConnection getFbConnection() {
		return fbConnection;
	}

	public void setFbConnection(FbConnection fbConnection) {
		this.fbConnection = fbConnection;
	}

	/* (non-Javadoc)
	 * @see org.krakenapps.socialweb.facebook.jsonobject.FacebookGraphObject#parseJson(org.json.JSONObject, java.util.Set)
	 */
	@Override
	public int parseJson(JSONObject json, Set<Permissions> permit) {
		// TODO Auto-generated method stub
		return 0;
	}

}

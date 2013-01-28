package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class Offer implements FacebookGraphObject{

	private String id;
	private From from;
	private String title;
	private String created_time;
	private String expiration_time;
	private String terms;
	private String image_url;	
	private String coupon_type;	
	private int claim_limit;	
	private String redemption_link;	
	private String redemption_code;	
	FbConnection fbConnection;
	private class FbConnection{
		public FbConnection(){
			
		}
		//there is no Connections;
	}
	public Offer(){
		fbConnection = new FbConnection();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public From getFrom() {
		return from;
	}

	public void setFrom(From from) {
		this.from = from;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCreated_time() {
		return created_time;
	}

	public void setCreated_time(String created_time) {
		this.created_time = created_time;
	}

	public String getExpiration_time() {
		return expiration_time;
	}

	public void setExpiration_time(String expiration_time) {
		this.expiration_time = expiration_time;
	}

	public String getTerms() {
		return terms;
	}

	public void setTerms(String terms) {
		this.terms = terms;
	}

	public FbConnection getFbConnection() {
		return fbConnection;
	}

	public void setFbConnection(FbConnection fbConnection) {
		this.fbConnection = fbConnection;
	}
	
	@Override
	public int parseJson(JSONObject json) {
		try {
			id = json.getString("id");
			JSONObject fromObject = json.getJSONObject("from");
			from = new From(fromObject.getString("id"),fromObject.getString("name"));
			title = json.getString("title");
			created_time = json.getString("created_time");
			expiration_time = json.getString("expiration_time");
			terms = json.getString("terms");
			image_url = json.getString("image_url");
			coupon_type = json.getString("coupon_type");
			claim_limit = json.getInt("claim_limit");
			redemption_link = json.getString("redemption_link");
			redemption_code = json.getString("redemption_code");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
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

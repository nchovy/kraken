package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class Order implements FacebookGraphObject{

	private String id;
	private String from;
	private int amount;
	private String status; // settled, disputed, refunded, cancelled
	private From application;
	private String country;
	private String refund_reason_code;
	private String created_time;
	private String updated_time;
	FbConnection fbConnection;
	
	private class FbConnection{
		public String CONN_placed = "placed";
		public String CONN_settled = "settled";
		public String CONN_refunded = "refunded";
		public String CONN_disputed = "disputed";
		public String CONN_cancelled = "cancelled";
		//TODO : define parameter
		public FbConnection(){
			
		}
	}

	public Order(){
		fbConnection = new FbConnection();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public From getApplication() {
		return application;
	}
	public void setApplication(From application) {
		this.application = application;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getRefund_reason_code() {
		return refund_reason_code;
	}
	public void setRefund_reason_code(String refund_reason_code) {
		this.refund_reason_code = refund_reason_code;
	}
	public String getCreated_time() {
		return created_time;
	}
	public void setCreated_time(String created_time) {
		this.created_time = created_time;
	}
	public String getUpdated_time() {
		return updated_time;
	}
	public void setUpdated_time(String updated_time) {
		this.updated_time = updated_time;
	}
	@Override
	public int parseJson(JSONObject json) {
		try {
			id = json.getString("id");
			from = json.getString("from");
			amount = json.getInt("amount");
			status = json.getString("status");
			
			JSONObject applicationObject = json.getJSONObject("application");
			application = new From(applicationObject.getString("id"),applicationObject.getString("name"));
			
			country = json.getString("country");
			refund_reason_code = json.getString("refund_reason_code");
			created_time = json.getString("created_time");
			updated_time = json.getString("updated_time");
			
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

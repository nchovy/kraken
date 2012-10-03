package org.krakenapps.socialweb.facebook.jsonobject;

import org.json.JSONObject;


public class Order implements FacebookGraphObject{

	private String id;
	private String from;
	private int amount;
	private String status; // settled, disputed, refunded, cancelled
	private String application;
	private String country;
	private String refund_reason_code;
	private String created_time;
	private String updated_time;
	
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
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
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

	
}

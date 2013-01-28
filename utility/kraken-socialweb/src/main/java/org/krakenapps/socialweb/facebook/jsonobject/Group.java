package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;


public class Group implements FacebookGraphObject{
	
	private String id;
	private int version; // 0 = old type, 1 - Current group , 2 - 3 Top-level school group
	private String icon;
	private From owner;
	private String name;
	private String description;
	private String link;
	private String privacy;
	private String updated_time;
	private FbConnection fbConnection;
	public Group(){
		fbConnection = new FbConnection();
	}
	private class FbConnection{
		public FbConnection(){
			
		}
		public String CONN_events = "events";
		public String CONN_feed = "feed";
		public String CONN_members = "members";
		public String CONN_picture = "picture";
		public String CONN_docs = "docs";
		//TODO : object define
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public From getOwner() {
		return owner;
	}
	public void setOwner(From owner) {
		this.owner = owner;
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
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getPrivacy() {
		return privacy;
	}
	public void setPrivacy(String privacy) {
		this.privacy = privacy;
	}
	public String getUpdated_time() {
		return updated_time;
	}
	public void setUpdated_time(String updated_time) {
		this.updated_time = updated_time;
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
	@Override
	public int parseJson(JSONObject json) {
		try {
			id = json.getString("id");
			version = json.getInt("version");
			icon = json.getString("icon");
			
			JSONObject fromObject = json.getJSONObject("from");
			owner = new From(fromObject.getString("id"), fromObject.getString("name"));
			
			name = json.getString("name");
			description = json.getString("description");
			link =  json.getString("link");
			privacy = json.getString("privacy");
			updated_time = json.getString("updated_time");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

}

package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.Like;

public class Video implements FacebookGraphObject{
	private String id;
	private From from;
	private ArrayList<From> tags; 
	private String  name;
	private String description;
	private String picture;
	private String embed_html;
	private String icon;
	private String source;
	private String created_time;
	private String updated_time;
	private ArrayList<Comment> comments;
	private FbConnection fbConnection;
	
	Video(){
		fbConnection = new FbConnection(); 
	}
	private class FbConnection{
		public String CONN_likes = "likes";
		public String CONN_comments = "comments";
		public String CONN_picture = "picture";
		private Like likes;
		private ArrayList<Comment> comments;
		private String picture;
		public FbConnection(){
			likes = null;
			comments = null;
			picture = null;
		}
		public Like getLikes() {
			return likes;
		}
		public void setLikes(Like likes) {
			this.likes = likes;
		}
		public ArrayList<Comment> getComments() {
			return comments;
		}
		public void setComments(ArrayList<Comment> comments) {
			this.comments = comments;
		}
		public String getPicture() {
			return picture;
		}
		public void setPicture(String picture) {
			this.picture = picture;
		}
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

	public ArrayList<From> getTags() {
		return tags;
	}

	public void setTags(ArrayList<From> tags) {
		this.tags = tags;
	}

	public ArrayList<Comment> getComments() {
		return comments;
	}

	public void setComments(ArrayList<Comment> comments) {
		this.comments = comments;
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

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getEmbed_html() {
		return embed_html;
	}

	public void setEmbed_html(String embed_html) {
		this.embed_html = embed_html;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
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
			from = new From(fromObject.getString("id"), fromObject.getString("name"));
			
			JSONObject tagsObject = json.getJSONObject("tags");
			JSONArray tagsArray = tagsObject.getJSONArray("data");
			ArrayList<From> tagsList = new ArrayList<From>(tagsArray.length());
			for(int i=0; i<tagsArray.length(); i++){
				tags.add(new From(tagsArray.getJSONObject(i).getString("id"),tagsArray.getJSONObject(i).getString("name")));
			}
			
			name = json.getString("name");
			description = json.getString("ndescription");
			picture = json.getString("picture");
			embed_html = json.getString("embed_html");
			icon = json.getString("icon");
			source = json.getString("source");
			created_time = json.getString("created_time");
			updated_time = json.getString("updated_time");
			
			comments = new ArrayList<Comment>();
			JSONObject commentObject = json.getJSONObject("comments");
			JSONArray commentArray = commentObject.getJSONArray("data");
			for(int i =0 ; i<commentObject.getInt("count"); i++){
				Comment tmp = new Comment();
				tmp.setId(commentArray.getJSONObject(i).getString("id"));
				tmp.setFrom(new From(commentArray.getJSONObject(i).getJSONObject("from").getString("id"), commentArray.getJSONObject(i).getJSONObject("from").getString("name")));
				tmp.setMessage(commentArray.getJSONObject(i).getString("message"));
				tmp.setCreateTime(commentArray.getJSONObject(i).getString("created_time"));
				comments.add(tmp);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
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

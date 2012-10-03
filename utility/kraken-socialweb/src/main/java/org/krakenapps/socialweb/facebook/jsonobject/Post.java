package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.Like;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.Place;


/**
 * @author hefos
 *
 */
public class Post implements FacebookGraphObject{

	private String id;
	private From from;
	private ArrayList<From> to;
	private String message;
	private ArrayList<From> message_tag;
	private String picture;
	private String link;
	private String name;
	private String caption;
	private String description;
	private String source;
	private FbConnection fbConnection;
	private Property properties;
	private String icon;
	private Action actions;
	private String privacy;
	private String type;
	private Like likes;
	private Place place;
	private String story;
	private ArrayList<From> story_tags;
	private ArrayList<From> with_tags;
	private ArrayList<Comment> comments;
	private int object_id;
	private From application;
	private String created_time;
	private String updated_time;
	
	private class FbConnection{
		public String CONN_comments = "comments";
		public String CONN_likes = "likes";
		public String CONN_insights = "insights";
		private ArrayList<Comment> comments;
		private Like likes;
		private Insight insights;
		public FbConnection(){
			comments = null;
			likes = null;
			insights = null;
		}
		public ArrayList<Comment> getComments() {
			return comments;
		}
		public void setComments(ArrayList<Comment> comments) {
			this.comments = comments;
		}
		public Like getLikes() {
			return likes;
		}
		public void setLikes(Like likes) {
			this.likes = likes;
		}
		public Insight getInsights() {
			return insights;
		}
		public void setInsights(Insight insights) {
			this.insights = insights;
		}
		
	}
	private class Property{
		private String id;
		private String text;
		public Property(){
			
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
	}
	private class Action{
		private String name;
		private String link;
		public Action(){
			
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getLink() {
			return link;
		}
		public void setLink(String link) {
			this.link = link;
		}
	}
	public Post(){
		fbConnection = new FbConnection();
		properties = new Property();
		likes = new Like();
		place = new Place();
		application = new From();
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


	public ArrayList<From> getTo() {
		return to;
	}


	public void setTo(ArrayList<From> to) {
		this.to = to;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public ArrayList<From> getMessage_tag() {
		return message_tag;
	}


	public void setMessage_tag(ArrayList<From> message_tag) {
		this.message_tag = message_tag;
	}


	public String getPicture() {
		return picture;
	}


	public void setPicture(String picture) {
		this.picture = picture;
	}


	public String getLink() {
		return link;
	}


	public void setLink(String link) {
		this.link = link;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getCaption() {
		return caption;
	}


	public void setCaption(String caption) {
		this.caption = caption;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getSource() {
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}


	public FbConnection getFbConnection() {
		return fbConnection;
	}


	public void setFbConnection(FbConnection fbConnection) {
		this.fbConnection = fbConnection;
	}


	public Property getProperties() {
		return properties;
	}


	public void setProperties(Property properties) {
		this.properties = properties;
	}


	public String getIcon() {
		return icon;
	}


	public void setIcon(String icon) {
		this.icon = icon;
	}


	public Action getActions() {
		return actions;
	}


	public void setActions(Action actions) {
		this.actions = actions;
	}


	public String getPrivacy() {
		return privacy;
	}


	public void setPrivacy(String privacy) {
		this.privacy = privacy;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public Like getLikes() {
		return likes;
	}


	public void setLikes(Like likes) {
		this.likes = likes;
	}


	public Place getPlace() {
		return place;
	}


	public void setPlace(Place place) {
		this.place = place;
	}


	public String getStory() {
		return story;
	}


	public void setStory(String story) {
		this.story = story;
	}


	public ArrayList<From> getStory_tags() {
		return story_tags;
	}


	public void setStory_tags(ArrayList<From> story_tags) {
		this.story_tags = story_tags;
	}


	public ArrayList<From> getWith_tags() {
		return with_tags;
	}


	public void setWith_tags(ArrayList<From> with_tags) {
		this.with_tags = with_tags;
	}


	public ArrayList<Comment> getComments() {
		return comments;
	}


	public void setComments(ArrayList<Comment> comments) {
		this.comments = comments;
	}


	public int getObject_id() {
		return object_id;
	}


	public void setObject_id(int object_id) {
		this.object_id = object_id;
	}


	public From getApplication() {
		return application;
	}


	public void setApplication(From application) {
		this.application = application;
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
		// TODO Auto-generated method stub
		return 0;
	}
	

}

package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.Like;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.Place;


/**
 * @author tgnice
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
	private ArrayList<Property> properties;
	private String icon;
	private ArrayList<Action> actions;
	private Privcy privacy;
	private String type;
	private Like likes;
	private Place place;
	private String story;
	private ArrayList<From> story_tags;
	private ArrayList<From> with_tags;
	private ArrayList<Comment> comments;
	private String object_id;
	private From application;
	private String created_time;
	private String updated_time;
	
	public Post(){
		fbConnection = new FbConnection();
		from = new From();
		message_tag = new ArrayList<From>();
		properties = new ArrayList<Post.Property>();
		likes = new Like();
		place = new Place();
		application = new From();
		actions = new ArrayList<Post.Action>();
		story_tags = new ArrayList<From>();
		with_tags = new ArrayList<From>();
		comments = new ArrayList<Comment>();
		application = new From();
		privacy = new Privcy();
	}
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
		public Property(String id, String text){
			this.id = id;
			this.text = text;
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
		public Action(String name , String link){
			this.name = name;
			this.link = link;
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
	private class Privcy{
		private String value;
		private String networks;
		private String allow;
		private String deny;
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getNetworks() {
			return networks;
		}
		public void setNetworks(String networks) {
			this.networks = networks;
		}
		public String getAllow() {
			return allow;
		}
		public void setAllow(String allow) {
			this.allow = allow;
		}
		public String getDeny() {
			return deny;
		}
		public void setDeny(String deny) {
			this.deny = deny;
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


	public ArrayList<Property> getProperties() {
		return properties;
	}


	public void setProperties(ArrayList<Property> properties) {
		this.properties = properties;
	}


	public String getIcon() {
		return icon;
	}


	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Privcy getPrivacy() {
		return privacy;
	}


	public void setPrivacy(Privcy privacy) {
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


	public ArrayList<Action> getActions() {
		return actions;
	}


	public void setActions(ArrayList<Action> actions) {
		this.actions = actions;
	}


	public String getObject_id() {
		return object_id;
	}


	public void setObject_id(String object_id) {
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
		try {
			id = json.getString("id");
			
			JSONObject fromObejct = json.getJSONObject("from");
			from.setId(fromObejct.getString("id"));
			from.setName(fromObejct.getString("name"));
			
			JSONObject toObject = json.getJSONObject("to");
			JSONArray toArray = toObject.getJSONArray("data");
			for(int i =0; i<toArray.length() ; i++){
				to.add(new From(toArray.getJSONObject(i).getString("id"),toArray.getJSONObject(i).getString("Name")));
			}
			
			message = json.getString("message");
			
			JSONObject messageTagObject = json.getJSONObject("message_tag");
			JSONArray messageTagArray = messageTagObject.getJSONArray("data");
			for(int i =0; i <messageTagArray.length() ; i++){
				message_tag.add(new From(messageTagArray.getJSONObject(i).getString("id"),messageTagArray.getJSONObject(i).getString("name")));
			}
			
			picture = json.getString("picture");
			link = json.getString("link");
			name = json.getString("name");
			caption = json.getString("caption");
			description = json.getString("description");
			source = json.getString("source");
			
			JSONObject propertyObject = json.getJSONObject("properties");
			JSONArray propertyArray = propertyObject.getJSONArray("data");
			for(int i = 0 ; i< propertyArray.length(); i++){
				properties.add(new Property(propertyArray.getJSONObject(i).getString("id"), propertyArray.getJSONObject(i).getString("text")));
			}
			
			icon = json.getString("icon");
			
			JSONObject actionObject = json.getJSONObject("actions");
			JSONArray actionArray = actionObject.getJSONArray("data");
			for(int i = 0 ; i< actionArray.length(); i++){
				actions.add(new Action(actionArray.getJSONObject(i).getString("name"),actionArray.getJSONObject(i).getString("link")));
			}
			
			JSONObject privacyObject = json.getJSONObject("privacy");
			privacy.setValue(privacyObject.getString("value"));
			if(privacy.getValue().equals("CUSTOM")){
				if(privacyObject.containsKey("allow")){
					privacy.setAllow(privacyObject.getString("allow"));
				}
				if(privacyObject.containsKey("deny")){
					privacy.setDeny(privacyObject.getString("deny"));
				}
			}
			
			type = json.getString("type");
			
			JSONObject likeObject = json.getJSONObject("likes");
			JSONArray likeArray = likeObject.getJSONArray("data");
			likes.setCount(likeObject.getInt("count"));
			ArrayList<From> likeList = new ArrayList<From>(likes.getCount());
			for(int i=0; i<likes.getCount(); i++){
				likeList.add(new From(likeArray.getJSONObject(i).getString("id"),likeArray.getJSONObject(i).getString("name")));
			}
			likes.setLikeList(likeList);
			
			JSONObject placeObject = json.getJSONObject("place");
			place = new Place(placeObject.getString("id"), placeObject.getString("name"), placeObject.getInt("longitude") , placeObject.getInt("latitude"));

			story = json.getString("story");

			JSONObject storyTagObject = json.getJSONObject("story_tag");
			JSONArray storyTagArray = storyTagObject.getJSONArray("data");
			for(int i =0; i <storyTagArray.length() ; i++){
				story_tags.add(new From(storyTagArray.getJSONObject(i).getString("id"),storyTagArray.getJSONObject(i).getString("name")));
			}
			
			JSONObject withTagObject = json.getJSONObject("with_tag");
			JSONArray withTagArray = withTagObject.getJSONArray("data");
			for(int i =0; i <withTagArray.length() ; i++){
				with_tags.add(new From(storyTagArray.getJSONObject(i).getString("id"),storyTagArray.getJSONObject(i).getString("name")));
			}
			
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
			
			object_id = json.getString("object_id");
			
			application.setId(json.getJSONObject("application").getString("id"));
			application.setName(json.getJSONObject("application").getString("name"));
			created_time = json.getString("created_time");
			updated_time = json.getString("updated_time");
			
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


package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.*;

public class Photo implements FacebookGraphObject{

	private String id;
	private From from;
	private ArrayList<Tag> tags; 
	private String name;
	private ArrayList<NameTag> name_tags;
	private String icon;
	private String picture;
	private String source;
	private int height;
	private int width;
	private ArrayList<Image> images;
	private String link;
	private Place place;
	private String created_time;
	private String updated_time;
	private int position;
	private FbConnection fbConnection;
	private class FbConnection{
		public String CONN_comments = "comments";
		public String CONN_likes = "likes";
		public String CONN_picture = "picture";
		public String CONN_tags = "tags";
		private ArrayList<Comment> comments;
		private Like likes;
		private String picture;
		private ArrayList<Tag> tags;
		private class Tag{
			private String id;
			private String name;
		}
		public FbConnection(){
			comments = null;
			likes = null;
			tags = null;
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
		public String getPicture() {
			return picture;
		}
		public void setPicture(String picture) {
			this.picture = picture;
		}
		public ArrayList<Tag> getTags() {
			return tags;
		}
		public void setTags(ArrayList<Tag> tags) {
			this.tags = tags;
		}
		
	}
	private class Tag{
		private int x;
		private int y;
		public Tag(){
			
		}
		public Tag(int x , int y){
			this.x = x;
			this.y = y;
		}
		public int getX() {
			return x;
		}
		public void setX(int x) {
			this.x = x;
		}
		public int getY() {
			return y;
		}
		public void setY(int y) {
			this.y = y;
		}
	}
	private class NameTag{
		private String id;
		private String name;
		private String type;
		public NameTag(){
			
		}
		public NameTag(String id , String name, String type){
			this.id = id;
			this.name = name;
			this.type = type;
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
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}
	private class Image{
		private String source;
		private int height;
		private int width;
		public Image(){
			
		}
		public Image(String source , int height, int width){
			this.source = source;
			this.height = height;
			this.width = width;
		}
		public String getSource() {
			return source;
		}
		public void setSource(String source) {
			this.source = source;
		}
		public int getHeight() {
			return height;
		}
		public void setHeight(int height) {
			this.height = height;
		}
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}
	}
	
	public Photo(){
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

	public ArrayList<Tag> getTags() {
		return tags;
	}

	public void setTags(ArrayList<Tag> tags) {
		this.tags = tags;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<NameTag> getName_tags() {
		return name_tags;
	}

	public void setName_tags(ArrayList<NameTag> name_tags) {
		this.name_tags = name_tags;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public ArrayList<Image> getImages() {
		return images;
	}

	public void setImages(ArrayList<Image> images) {
		this.images = images;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
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

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public int parseJson(JSONObject json) {
		try {
			id = json.getString("id");
			JSONObject fromObject = json.getJSONObject("from");
			from = new From(fromObject.getString("id"),fromObject.getString("name"));

			JSONObject tagsObject = json.getJSONObject("tags");
			JSONArray tagsArray = tagsObject.getJSONArray("data");
			ArrayList<Tag> tagsList = new ArrayList<Tag>(tagsArray.length());
			for(int i=0; i<tagsArray.length(); i++){
				tags.add(new Tag(tagsArray.getJSONObject(i).getInt("x"),tagsArray.getJSONObject(i).getInt("y")));
			}
			
			name = json.getString("name");
			JSONObject nameTagObject = json.getJSONObject("name_tags");
			JSONArray nameTagArray = nameTagObject.getJSONArray("data");
			ArrayList<NameTag> nameTagList = new ArrayList<Photo.NameTag>(nameTagArray.length());
			for(int i=0; i< nameTagArray.length(); i++){
				nameTagList.add(new NameTag(nameTagArray.getJSONObject(i).getString("id"),nameTagArray.getJSONObject(i).getString("name"), nameTagArray.getJSONObject(i).getString("type")));
			}
			name_tags = nameTagList;
			
			icon = json.getString("icon");
			picture = json.getString("picture");
			source = json.getString("source");
			height = json.getInt("height");
			width = json.getInt("width");
			
			JSONObject imagesObejct = json.getJSONObject("images");
			JSONArray imagesArray = imagesObejct.getJSONArray("data");
			ArrayList<Image> imagesList= new ArrayList<Image>(imagesArray.length());
			for(int i=0; i< imagesArray.length(); i++){
				imagesList.add(new Image(imagesArray.getJSONObject(i).getString("source"), imagesArray.getJSONObject(i).getInt("height"), imagesArray.getJSONObject(i).getInt("width")));
			}
			images = imagesList;
			
			link = json.getString("link");
			
			JSONObject placeObject = json.getJSONObject("place");
			place = new Place(placeObject.getString("id"), placeObject.getString("name"), placeObject.getInt("longitude") , placeObject.getInt("latitude"));
			
			created_time = json.getString("created_time");
			updated_time = json.getString("updated_time");
			position = json.getInt("position");
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

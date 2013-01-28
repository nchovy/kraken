package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.From;

public class Page implements FacebookGraphObject{

	private String id;
	private String name;
	private String link;
	private String category;
	private boolean is_published;
	private boolean can_post;
	private int likes;
	//private Dictionary location;
	private String phone;
	private int checkins;
	private String picture;
	private String cover;
	private String website;
	private int talking_about_count;
	//private Dictionary global_brand_parent_page;
	private String access_token;
	private FbConnection fbConnection;
	
	private class FbConnection{
		public String CONN_feed = "feed";
		public String CONN_picture = "picture";
		public String CONN_settings = "settings";
		public String CONN_tagged = "tagged";
		public String CONN_link = "link";
		public String CONN_photos = "photos";
		public String CONN_groups = "groups";
		public String CONN_albums = "albums";
		public String CONN_statues = "statues";
		public String CONN_notes = "notes";
		public String CONN_posts = "posts";
		public String CONN_promotable_posts = "promotable_posts";
		public String CONN_questions = "questions";
		public String CONN_events = "events";
		public String CONN_checkins = "checkins";
		public String CONN_admins = "admins";
		public String CONN_conversations = "conversations";
		public String CONN_milestones = "milestones";
		public String CONN_blocked = "blocked";
		public String CONN_tabs = "tabs";
		public String CONN_insights = "insights";
		private ArrayList<Post> feed;
		private String picture;
		private ArrayList<Settings> settings;
		private ArrayList<FacebookGraphObject> tagged;
		private ArrayList<Link> link;
		private ArrayList<Photo> photos;
		private ArrayList<Group> groups;
		private ArrayList<Album> albums;
		private ArrayList<StatusMessage> statuses;
		private ArrayList<Video> videos;
		private ArrayList<Note> notes;
		private ArrayList<Post> posts;
		private ArrayList<Post> promotable_posts;
		private ArrayList<Question> quetions;
		private ArrayList<Event> events;
		private ArrayList<Checkin> checkins;
		private ArrayList<From> admins;
		private ArrayList<Conversation> conversations;
		private ArrayList<Milestone> milestones;
		private ArrayList<From> blocked;
		private ArrayList<Tab> tabs;
		private ArrayList<Insight> insights;
		private class Settings{
			private String setting;
			private String value;
			public Settings(){
				
			}
			public String getSetting() {
				return setting;
			}
			public void setSetting(String setting) {
				this.setting = setting;
			}
			public String getValue() {
				return value;
			}
			public void setValue(String value) {
				this.value = value;
			}
			
		}		
		private class Conversation{
			private String id;
			private String snippet;
			private String updated_time;
			private int message_count;
			private int unread_count;
			private ArrayList<String> tags;
			private ArrayList<ParticipantsAndSenders> participants;
			private ArrayList<ParticipantsAndSenders> senders;
			private boolean can_reply;
			private boolean is_cubscribed;
			private ArrayList<Message> messages;
			public Conversation(){
				
			}
			
			private class ParticipantsAndSenders{
				private String name;
				private String email;
				private String id;
				public ParticipantsAndSenders(){
					
				}
				public String getName() {
					return name;
				}
				public void setName(String name) {
					this.name = name;
				}
				public String getEmail() {
					return email;
				}
				public void setEmail(String email) {
					this.email = email;
				}
				public String getId() {
					return id;
				}
				public void setId(String id) {
					this.id = id;
				}
				
			}

			public String getId() {
				return id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public String getSnippet() {
				return snippet;
			}

			public void setSnippet(String snippet) {
				this.snippet = snippet;
			}

			public String getUpdated_time() {
				return updated_time;
			}

			public void setUpdated_time(String updated_time) {
				this.updated_time = updated_time;
			}

			public int getMessage_count() {
				return message_count;
			}

			public void setMessage_count(int message_count) {
				this.message_count = message_count;
			}

			public int getUnread_count() {
				return unread_count;
			}

			public void setUnread_count(int unread_count) {
				this.unread_count = unread_count;
			}

			public ArrayList<String> getTags() {
				return tags;
			}

			public void setTags(ArrayList<String> tags) {
				this.tags = tags;
			}

			public ArrayList<ParticipantsAndSenders> getParticipants() {
				return participants;
			}

			public void setParticipants(ArrayList<ParticipantsAndSenders> participants) {
				this.participants = participants;
			}

			public ArrayList<ParticipantsAndSenders> getSenders() {
				return senders;
			}

			public void setSenders(ArrayList<ParticipantsAndSenders> senders) {
				this.senders = senders;
			}

			public boolean isCan_reply() {
				return can_reply;
			}

			public void setCan_reply(boolean can_reply) {
				this.can_reply = can_reply;
			}

			public boolean isIs_cubscribed() {
				return is_cubscribed;
			}

			public void setIs_cubscribed(boolean is_cubscribed) {
				this.is_cubscribed = is_cubscribed;
			}

			public ArrayList<Message> getMessages() {
				return messages;
			}

			public void setMessages(ArrayList<Message> messages) {
				this.messages = messages;
			}
		}
		private class Milestone{
			private String id;
			private String title;
			private From from;
			private String description;
			private String created_time;
			private String updated_time;
			private String start_time;
			private String end_time;
			public Milestone(){
				from = null;
			}
			public String getId() {
				return id;
			}
			public void setId(String id) {
				this.id = id;
			}
			public String getTitle() {
				return title;
			}
			public void setTitle(String title) {
				this.title = title;
			}
			public From getFrom() {
				return from;
			}
			public void setFrom(From from) {
				this.from = from;
			}
			public String getDescription() {
				return description;
			}
			public void setDescription(String description) {
				this.description = description;
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
			public String getStart_time() {
				return start_time;
			}
			public void setStart_time(String start_time) {
				this.start_time = start_time;
			}
			public String getEnd_time() {
				return end_time;
			}
			public void setEnd_time(String end_time) {
				this.end_time = end_time;
			}
		}
		private class Tab{
			private String id;
			private String name;
			private String link;
			private From application;
			private String custom_name;
			private boolean is_permanent;
			private int position;
			private boolean is_non_connection_landing_tab;
			public Tab(){
				application =null;
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
			public String getLink() {
				return link;
			}
			public void setLink(String link) {
				this.link = link;
			}
			public From getApplication() {
				return application;
			}
			public void setApplication(From application) {
				this.application = application;
			}
			public String getCustom_name() {
				return custom_name;
			}
			public void setCustom_name(String custom_name) {
				this.custom_name = custom_name;
			}
			public boolean isIs_permanent() {
				return is_permanent;
			}
			public void setIs_permanent(boolean is_permanent) {
				this.is_permanent = is_permanent;
			}
			public int getPosition() {
				return position;
			}
			public void setPosition(int position) {
				this.position = position;
			}
			public boolean isIs_non_connection_landing_tab() {
				return is_non_connection_landing_tab;
			}
			public void setIs_non_connection_landing_tab(
					boolean is_non_connection_landing_tab) {
				this.is_non_connection_landing_tab = is_non_connection_landing_tab;
			}
		}
		public FbConnection(){
			feed = null;
			picture = null;
			settings = null;
			tagged = null;
			link = null;
			photos = null;
			groups = null;
			albums = null;
			statuses = null;
			videos = null;
			notes = null;
			posts = null;
			promotable_posts = null;
			quetions = null;
			events = null;
			checkins = null;
			admins = null;
			conversations = null;
			milestones = null;
			blocked = null;
			tabs = null;
			insights = null;
		}
		public ArrayList<Post> getFeed() {
			return feed;
		}
		public void setFeed(ArrayList<Post> feed) {
			this.feed = feed;
		}
		public String getPicture() {
			return picture;
		}
		public void setPicture(String picture) {
			this.picture = picture;
		}
		public ArrayList<Settings> getSettings() {
			return settings;
		}
		public void setSettings(ArrayList<Settings> settings) {
			this.settings = settings;
		}
		public ArrayList<FacebookGraphObject> getTagged() {
			return tagged;
		}
		public void setTagged(ArrayList<FacebookGraphObject> tagged) {
			this.tagged = tagged;
		}
		public ArrayList<Link> getLink() {
			return link;
		}
		public void setLink(ArrayList<Link> link) {
			this.link = link;
		}
		public ArrayList<Photo> getPhotos() {
			return photos;
		}
		public void setPhotos(ArrayList<Photo> photos) {
			this.photos = photos;
		}
		public ArrayList<Group> getGroups() {
			return groups;
		}
		public void setGroups(ArrayList<Group> groups) {
			this.groups = groups;
		}
		public ArrayList<Album> getAlbums() {
			return albums;
		}
		public void setAlbums(ArrayList<Album> albums) {
			this.albums = albums;
		}
		public ArrayList<StatusMessage> getStatuses() {
			return statuses;
		}
		public void setStatuses(ArrayList<StatusMessage> statuses) {
			this.statuses = statuses;
		}
		public ArrayList<Video> getVideos() {
			return videos;
		}
		public void setVideos(ArrayList<Video> videos) {
			this.videos = videos;
		}
		public ArrayList<Note> getNotes() {
			return notes;
		}
		public void setNotes(ArrayList<Note> notes) {
			this.notes = notes;
		}
		public ArrayList<Post> getPosts() {
			return posts;
		}
		public void setPosts(ArrayList<Post> posts) {
			this.posts = posts;
		}
		public ArrayList<Post> getPromotable_posts() {
			return promotable_posts;
		}
		public void setPromotable_posts(ArrayList<Post> promotable_posts) {
			this.promotable_posts = promotable_posts;
		}
		public ArrayList<Question> getQuetions() {
			return quetions;
		}
		public void setQuetions(ArrayList<Question> quetions) {
			this.quetions = quetions;
		}
		public ArrayList<Event> getEvents() {
			return events;
		}
		public void setEvents(ArrayList<Event> events) {
			this.events = events;
		}
		public ArrayList<Checkin> getCheckins() {
			return checkins;
		}
		public void setCheckins(ArrayList<Checkin> checkins) {
			this.checkins = checkins;
		}
		public ArrayList<From> getAdmins() {
			return admins;
		}
		public void setAdmins(ArrayList<From> admins) {
			this.admins = admins;
		}
		public ArrayList<Milestone> getMilestones() {
			return milestones;
		}
		public void setMilestones(ArrayList<Milestone> milestones) {
			this.milestones = milestones;
		}
		public ArrayList<From> getBlocked() {
			return blocked;
		}
		public void setBlocked(ArrayList<From> blocked) {
			this.blocked = blocked;
		}
		public ArrayList<Tab> getTabs() {
			return tabs;
		}
		public void setTabs(ArrayList<Tab> tabs) {
			this.tabs = tabs;
		}
		public ArrayList<Insight> getInsights() {
			return insights;
		}
		public void setInsights(ArrayList<Insight> insights) {
			this.insights = insights;
		}
	}
	public Page(){
		fbConnection = new FbConnection();
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

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public boolean isIs_published() {
		return is_published;
	}

	public void setIs_published(boolean is_published) {
		this.is_published = is_published;
	}

	public boolean isCan_post() {
		return can_post;
	}

	public void setCan_post(boolean can_post) {
		this.can_post = can_post;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}


	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getCheckins() {
		return checkins;
	}

	public void setCheckins(int checkins) {
		this.checkins = checkins;
	}

	public String getPictures() {
		return picture;
	}

	public void setPictures(String pictures) {
		this.picture = pictures;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public int getTalking_about_count() {
		return talking_about_count;
	}

	public void setTalking_about_count(int talking_about_count) {
		this.talking_about_count = talking_about_count;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	@Override
	public int parseJson(JSONObject json) {
		try {
			id = json.getString("id");
			name = json.getString("name");
			link = json.getString("link");
			category = json.getString("category");
			is_published = json.getBoolean("is_published");
			can_post = json.getBoolean("can_post");
			likes = json.getInt("likes");
			//TODO
			JSONObject locationObject = json.getJSONObject("location");
			
			phone = json.getString("phone");
			checkins = json.getInt("checkins");
			picture = json.getString("picture");
			website = json.getString("website");
			talking_about_count = json.getInt("talking_about_count");
			//TODO
			JSONObject globalObject = json.getJSONObject("global_brand_parent_page");
			access_token = json.getString("access_token");
			
			JSONObject hoursObject = json.getJSONObject("hours");
			//TODO
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
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



package org.krakenapps.socialweb.facebook.graphapi.objectcode;

import java.util.HashMap;
import java.util.Map;

public enum Permissions {
	/*	permission rules
	 * 00 00 00 
	 * 12 34 56
	 * this pattern that is not related with Facebook is made by tgnice. just patterned in kraken. 
	 * 1-2 this field represent kind of permissions in category (1)0x10---- is user and friend , (2) 0x20---- is extended permissions, (3) 0x30 is  open-graph permissions , (4) 0x40 is page permission 
	 * 
	 * 3-4 this field represent kind of permissions in user and other (1)0x--10-- is user (2) 0x--20-- is friends (3) 0x0--30-- is both of user and friends
	 * 
	 * 5-6 this field represent sequence
	 *  
	 *  for example 0x101001 is represented as first sequence user has permission that user permissions among "user and friend" category.
	 *              0x202002 is represented as second sequence user has permission that friends permissions among "user and friend" category.    
	 * 
	 * */
	user_about_me(0x101001),
	friends_about_me(0x102001),	//	Provides access to the "About Me" section of the profile in the about property
	user_activities(0x101002),
	friends_activities(0x102002),	//Provides access to the user's list of activities as the activities connection
	user_birthday(0x101003),	
	friends_birthday(0x102003),	//Provides access to the birthday with year as the birthday property
	user_checkins(0x101004),	
	friends_checkins(0x102004),	//Provides read access to the authorized user's check-ins or a friend's check-ins that the user can see. This permission is superseded by user_status for new applications as of March, 2012.
	user_education_history(0x101005),	
	friends_education_history(0x102005),	//Provides access to education history as the education property
	user_events(0x101006),	
	friends_events(0x102006),	//Provides access to the list of events the user is attending as the events connection
	user_groups(0x101007),	
	friends_groups(0x102007),	//Provides access to the list of groups the user is a member of as the groups connection
	user_hometown(0x101008),	
	friends_hometown(0x102008),	//Provides access to the user's hometown in the hometown property
	user_interests(0x101009),	
	friends_interests(0x102009),	//Provides access to the user's list of interests as the interests connection
	user_likes(0x10100A),	
	friends_likes(0x10200A),	//Provides access to the list of all of the pages the user has liked as the likes connection
	user_location(0x10100B),	
	friends_location(0x10200B),	//Provides access to the user's current location as the location property
	user_notes(0x10100C),	
	friends_notes(0x10200C),	//Provides access to the user's notes as the notes connection
	user_photos(0x10100D),	
	friends_photos(0x10200D),	//Provides access to the photos the user has uploaded, and photos the user has been tagged in
	user_questions(0x10100E),	
	friends_questions(0x10200E),	//Provides access to the questions the user or friend has asked
	user_relationships(0x101010),	
	friends_relationships(0x102010),	//Provides access to the user's family and personal relationships and relationship status
	user_relationship_details(0x101011),
	friends_relationship_details(0x102011),	//Provides access to the user's relationship preferences
	user_religion_politics(0x101012),	
	friends_religion_politics(0x102012),	//Provides access to the user's religious and political affiliations
	user_status	(0x101013),
	friends_status(0x102013),	//Provides access to the user's status messages and checkins. Please see the documentation for the location_post table for information on how this permission may affect retrieval of information about the locations associated with posts.
	user_subscriptions(0x101014),	
	friends_subscriptions(0x102014),	//Provides access to the user's subscribers and subscribees
	user_videos(0x101015),	
	friends_videos(0x102015),	//Provides access to the videos the user has uploaded, and videos the user has been tagged in
	user_website(0x101016),	
	friends_website(0x102016),	//Provides access to the user's web site URL
	user_work_history(0x101017),
	friends_work_history(0x102017),	//Provides access to work history as the work property
	email(0x101018),	//N/A	Provides access to the user's primary email address in the email property. Do not spam users. Your use of email must comply both with Facebook policies and with the CAN-SPAM Act.
	
	read_friendlists(0x201001),	//Provides access to any friend lists the user created. All user's friends are provided as part of basic data, this extended permission grants access to the lists of friends a user has created, and should only be requested if your application utilizes lists of friends.
	read_insights(0x201002),	//Provides read access to the Insights data for pages, applications, and domains the user owns.
	read_mailbox(0x201003),	//Provides the ability to read from a user's Facebook Inbox.
	read_requests(0x201004),	//Provides read access to the user's friend requests
	read_stream(0x201005),	//Provides access to all the posts in the user's News Feed and enables your application to perform searches against the user's News Feed
	xmpp_login(0x201006),	//Provides applications that integrate with Facebook Chat the ability to log in users.
	ads_management(0x201007),	//Provides the ability to manage ads and call the Facebook Ads API on behalf of a user.
	create_event(0x201008),	//Enables your application to create and modify events on the user's behalf
	manage_friendlists(0x201009),	//Enables your app to create and edit the user's friend lists.
	manage_notifications(0x20100A),	//Enables your app to read notifications and mark them as read. Intended usage: This permission should be used to let users read and act on their notifications; it should not be used to for the purposes of modeling user behavior or data mining. Apps that misuse this permission may be banned from requesting it.
	user_online_presence(0x20100B),	//Provides access to the user's online/offline presence
	friends_online_presence(0x202001),	//Provides access to the user's friend's online/offline presence
	publish_checkins(0x20100C),	//Enables your app to perform checkins on behalf of the user.
	publish_stream(0x203001),	//Enables your app to post content, comments, and likes to a user's stream and to the streams of the user's friends. This is a superset publishing permission which also includes publish_actions. However, please note that Facebook recommends a user-initiated sharing model. Please read the Platform Policies to ensure you understand how to properly use this permission. Note, you do not need to request the publish_stream permission in order to use the Feed Dialog, the Requests Dialog or the Send Dialog.
	rsvp_event(0x20100D),	//Enables your application to RSVP to events on the user's behalf
	
	publish_actions(0x301001),	     //N/A	Allows your app to publish to the Open Graph using Built-in Actions, Achievements, Scores, or Custom Actions. Your app can also publish other activity which is detailed in the Publishing Permissions doc. Note: The user-prompt for this permission will be displayed in the first screen of the Enhanced Auth Dialog and cannot be revoked as part of the authentication flow. However, a user can later revoke this permission in their Account Settings. If you want to be notified if this happens, you should subscribe to the permissions object within the Realtime API.
	user_actions_music(0x301002),	
	friends_actions_music(0x302001),	//Allows you to retrieve the actions published by all applications using the built-in music.listens action.
	user_actions_news(0x301003),	
	friends_actions_news(0x302002),	//Allows you to retrieve the actions published by all applications using the built-in news.reads action.
	user_actions_video(0x301004),	
	friends_actions_video(0x302003),	//Allows you to retrieve the actions published by all applications using the built-in video.watches action.
	user_actions(0x301005), //	
	friends_actions(0x302004),	//Allows you retrieve the actions published by another application as specified by the app namespace. For example, to request the ability to retrieve the actions published by an app which has the namespace awesomeapp, prompt the user for the users_actions:awesomeapp and/or friends_actions:awesomeapp permissions.
	user_games_activity(0x301006),	
	friends_games_activity(0x302005),	//Allows you post and retrieve game achievement activity.
	
	
	manage_pages(0x401001);	/*Enables your application to retrieve access_tokens for Pages and Applications that the user administrates. The access tokens can be queried by calling /<user_id>/accounts via the Graph API. This permission is only compatible with the Graph API, not the deprecated REST API. 
					See here for generating long-lived Page access tokens that do not expire after 60 days.*/

	private static Map<Integer, Permissions> codeMap = new HashMap<Integer, Permissions>();
	
	static {
		for (Permissions code : Permissions.values()) {
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode() {
		return permit;
	}
	public static Permissions parse(int code) {
		return codeMap.get(code);
	}
			
	Permissions(int code){
		this.permit = code;
	}
	private int permit;
}

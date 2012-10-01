package org.krakenapps.socialweb.facebook.graphapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.jsonobject.fieldelement.Restrictions;


public class Facebook {
// regular formula is https://graph.facebook.com/ + id + /object + ?access_token=YOUR_ACCESS_TOKEN
	String graph = "https://graph.facebook.com/";
	String search = "https://graph.facebook.com/search?";
	String fql = graph+"/fql?q=";
	String appId;
	String accessToken;
	String callbackURL;

	public Facebook(String app , String token , String URL){
		appId = app;
		accessToken = token;
		callbackURL = URL;
	}
	public JSONObject getBasicInfo(String userId, String objectId) throws IOException{
		URL url = new URL(graph+userId+"/"+objectId+"?access_token="+accessToken);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuffer bf = new StringBuffer();
		while(true){
			String tmp = br.readLine();
			if(tmp == null) 
				break;
			bf.append(tmp);
		}
		return new JSONObject(bf);
	}
	
	public JSONObject getBasicInfo(String userId) throws IOException, JSONException{
		URL url = new URL(graph+userId+"?access_token="+accessToken);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		StringBuffer me = new StringBuffer();
		while(true){
			String tmp = bf.readLine();
			if(tmp == null) 
				break;
			me.append(tmp);
		}
		new JSONObject(me);
		return new JSONObject(me);
	}
	/*
	 * use following getSpecificInfo function like this Facebook fb = new Facebook();
	 * fb.getSpecificInfo(Album.getId+"/"+Album.Connection.CONN_photos);
	 * fb.getSpecificInfo(Album.getId+"/"+Album.Connection.CONN_likes); 
	 * Album is object parsed json 
	 * */
	public JSONObject getSpecificInfo(String objectId) throws IOException{
		URL url = new URL(graph+objectId+"?access_token="+accessToken);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuffer bf = new StringBuffer();
		while(true){
			String tmp = br.readLine();
			if(tmp == null) 
				break;
			bf.append(tmp);
		}
		return new JSONObject(bf);
	}
	
	
	public JSONObject getPaging(String nextUrl) throws Exception{
		URL url = new URL(nextUrl);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		StringBuffer bf = new StringBuffer();
		while(true){
			String tmp = br.readLine();
			if(tmp == null) 
				break;
			bf.append(tmp);
		}
		return new JSONObject(bf);
	}
	
	@SuppressWarnings("restriction")
	public ArrayList<JSONArray> getPostComment(String postid) throws Exception{
		ArrayList<JSONArray> commentsArray = new ArrayList<JSONArray>();
		JSONObject tmpComments;
		String url = graph+""+postid+ "/comments?access_token="+accessToken;
		tmpComments = getPaging(url);
		commentsArray.add((JSONArray)tmpComments.get("data"));
		if(tmpComments.containsKey("paging")){
			while(((JSONObject)tmpComments.get("paging")).containsKey("next")){
				tmpComments = getPaging( ((JSONObject)tmpComments.get("paging")).getString("next") );
				if(((JSONArray)tmpComments.get("data")).length() >0){
					commentsArray.add((JSONArray)tmpComments.get("data"));
				}
			}
		}
		return commentsArray;
	}
	/*designed searching method*/
	
	public JSONObject getSearchResult(String searchCommend , String types) throws IOException{
		URL url = new URL(graph+"q="+searchCommend+"&type="+types +"?access_token="+accessToken);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		StringBuffer bf = new StringBuffer();
		while(true){
			String tmp = br.readLine();
			if(tmp == null) 
				break;
			bf.append(tmp);
		}
		new JSONObject(bf);
		return new JSONObject(bf);
	}
	
	//TODO fql design
	public JSONObject getFqlOnlinePresence(String userId) throws IOException{
		URL url = new URL(fql+"SELECT%20online_presence%20FROM%20user%20WHERE%20uid=me()&access_token="+accessToken);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		StringBuffer bf = new StringBuffer();
		while(true){
			String tmp = br.readLine();
			if(tmp == null) 
				break;
			bf.append(tmp);
		}
		new JSONObject(bf);
		return new JSONObject(bf);
	}
	//getter and setter
	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getCallbackURL() {
		return callbackURL;
	}

	public void setCallbackURL(String callbackURL) {
		this.callbackURL = callbackURL;
	}
	
	
}


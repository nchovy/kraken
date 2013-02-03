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
	String accessToken;
	String callbackURL;

	public Facebook( String token , String URL){
		accessToken = token;
		callbackURL = URL;
	}
	public JSONObject getBasicInfo(String userId, String objectId) throws IOException, JSONException{
		URL url = new URL(graph+userId+"/"+objectId+"?access_token="+accessToken);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuffer bf = new StringBuffer();
		while(true){
			String tmp = br.readLine();
			if(tmp == null) 
				break;
//			System.out.println(tmp);
			bf.append(tmp);
		}
		return new JSONObject(bf.toString());
	}
	
	public JSONObject getBasicInfo(String userId) throws IOException, JSONException{
		URL url = new URL(graph+userId+"?access_token="+accessToken);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		StringBuffer me = new StringBuffer();
		while(true){
			String tmp = br.readLine();
			if(tmp == null) 
				break;
//			System.out.println(tmp);
			me.append(tmp);
		}
		return new JSONObject(me.toString());
	}
	/*
	 * use following getSpecificInfo function like this Facebook fb = new Facebook();
	 * fb.getSpecificInfo(Album.getId()+"/"+Album.Connection.CONN_photos);
	 * fb.getSpecificInfo(Album.getId()+"/"+Album.Connection.CONN_likes); 
	 * Album is object parsed json 
	 * */
	public ArrayList<JSONArray> getSpecificInfo(String objectId) throws Exception{
		ArrayList<JSONArray> objectArray = new ArrayList<JSONArray>();
		JSONObject tmpObject;
		String url =graph+objectId+"?access_token="+accessToken;
		tmpObject = getPaging(url);
		objectArray.add((JSONArray)tmpObject.get("data"));
		if(tmpObject.containsKey("paging")){
			while(((JSONObject)tmpObject.get("paging")).containsKey("next")){
				tmpObject = getPaging( ((JSONObject)tmpObject.get("paging")).getString("next") );
				if(((JSONArray)tmpObject.get("data")).length() >0){
					objectArray.add((JSONArray)tmpObject.get("data"));
				}
			}
		}
		return objectArray;
	}
	
	public ArrayList<JSONArray> getSpecificInfo(String objectId , String connection) throws Exception{
		ArrayList<JSONArray> objectArray = new ArrayList<JSONArray>();
		JSONObject tmpObject;
		String url =graph+objectId+"/"+connection+"?access_token="+accessToken;
		tmpObject = getPaging(url);
		objectArray.add((JSONArray)tmpObject.get("data"));
		if(tmpObject.containsKey("paging")){
			while(((JSONObject)tmpObject.get("paging")).containsKey("next")){
				tmpObject = getPaging( ((JSONObject)tmpObject.get("paging")).getString("next") );
				if(((JSONArray)tmpObject.get("data")).length() >0){
					objectArray.add((JSONArray)tmpObject.get("data"));
				}
			}
		}
		return objectArray;
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
		return new JSONObject(bf.toString());
	}
	
	/*designed searching method*/
	
	public JSONObject getSearchResult(String searchCommend , String types) throws IOException, JSONException{
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
		return new JSONObject(bf.toString());
	}
	
	//TODO fql design
	public JSONObject getFqlOnlinePresence(String userId) throws IOException, JSONException{
		URL url = new URL(fql+"SELECT%20online_presence%20FROM%20user%20WHERE%20uid="+userId+"&access_token="+accessToken);
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
		return new JSONObject(bf.toString());
	}
	//getter and setter
	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
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


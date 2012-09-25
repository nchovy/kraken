package org.krakenapps.socialweb.facebook.graphapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;


public class Facebook {
// regular formula is https://graph.facebook.com/ + id + /object + ?access_token=YOUR_ACCESS_TOKEN
	String graph = "https://graph.facebook.com/";
	String fql = graph+"/fql?";
	String appId;
	String accessToken;
	String callbackURL;

	public Facebook(String app , String token , String URL){
		appId = app;
		accessToken = token;
		callbackURL = URL;
	}
	public JSONObject getInfo(String userId, String objectId) throws IOException{
		URL url = new URL(graph+userId+"/"+objectId+"?access_token="+accessToken);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuffer me = new StringBuffer();
		while(true){
			String tmp = br.readLine();
			if(tmp == null) 
				break;
			me.append(tmp);
		}
		return new JSONObject(me);
	}
	
	public JSONObject getInfo(String userId) throws IOException, JSONException{
		URL url = new URL(graph+userId+"?access_token="+accessToken);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		StringBuffer me = new StringBuffer();
		while(true){
			String tmp = br.readLine();
			if(tmp == null) 
				break;
			me.append(tmp);
		}
		new JSONObject(me);
		return new JSONObject(me);
	}
	
	public JSONObject getPaging(String nextUrl) throws Exception{
		URL url = new URL(nextUrl);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		StringBuffer me = new StringBuffer();
		while(true){
			String tmp = br.readLine();
			if(tmp == null) 
				break;
			me.append(tmp);
		}
		return new JSONObject(me);
	}
	
	
	//TODO fql design
	public JSONObject getFqlOnlinePresence(String userId) throws IOException{
		URL url = new URL(fql+"?q=SELECT%20online_presence%20FROM%20user%20WHERE%20uid=me()&access_token="+accessToken);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		StringBuffer me = new StringBuffer();
		while(true){
			String tmp = br.readLine();
			if(tmp == null) 
				break;
			me.append(tmp);
		}
		new JSONObject(me);
		return new JSONObject(me);
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

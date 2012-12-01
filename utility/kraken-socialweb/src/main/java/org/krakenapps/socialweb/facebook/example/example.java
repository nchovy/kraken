/**
 * 
 */
package org.krakenapps.socialweb.facebook.example;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.Facebook;
import org.krakenapps.socialweb.facebook.jsonobject.User;

/**
 * @author tgnice@nchovy.com
 *
 * you can get your access token in "https://developers.facebook.com/tools/explorer?method=GET&path=100002488995162%3Ffields%3Did%2Cname"
 */
public class example {
	public static void main(String[] argv) {
		String access_token= "";// YOUR ACCESS_TOKEN Here
		String callback_url = null;
		Facebook graph;
		JSONObject basicInfo;
		JSONObject feed;
		ArrayList<JSONArray> jsonArrayList;
		User user;
		
		graph = new Facebook( access_token, callback_url);
		/*usage*/
		try {
			basicInfo = graph.getBasicInfo("me" /* or YOUR_FACEBOK_ID*/); // you can access your basic profile on the Facebook. "me" is word already occupied in Facebook.
			// TODO here
			
			feed = graph.getBasicInfo("me", "feed");
			jsonArrayList = graph.getSpecificInfo(feed.getString("id"), "comments"); // or other things as following
			//jsonArrayList = graph.getSpecificInfo(feed.getString("id"), "likes"); 
			for(int index = 0; index < jsonArrayList.size(); index ++){
				JSONArray tmp = jsonArrayList.get(index);
				for(int i = 0 ; i<tmp.length(); i++){
					//TODO here
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

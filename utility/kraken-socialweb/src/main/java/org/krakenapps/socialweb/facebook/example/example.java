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
import org.krakenapps.socialweb.facebook.jsonobject.Comment;
import org.krakenapps.socialweb.facebook.jsonobject.User;

/**
 * @author tgnice@nchovy.com
 *
 * you can get your access token in "https://developers.facebook.com/tools/explorer?method=GET&path=100002488995162%3Ffields%3Did%2Cname"
 */
public class example {
	public static void main(String[] argv) {
		String access_token= ""; /* YOUR ACCESS_TOKEN Here*/
		String callback_url = null;
		Facebook graph;
		JSONObject basicInfo;
		JSONObject feed;
		ArrayList<JSONArray> jsonArrayList;
		
		graph = new Facebook( access_token, callback_url);
		/*usage*/
		try {
			basicInfo = graph.getBasicInfo("me" /* or YOUR_FACEBOK_ID*/); // you can access your basic profile on the Facebook. "me" is word already occupied in Facebook.
			// TODO here you can get your id, name, gender, age.. etc.
			System.out.println(basicInfo.getString("id"));
			
			feed = graph.getBasicInfo("me", "feed");
			jsonArrayList = graph.getSpecificInfo( ((JSONObject) (((JSONArray) feed.get("data")).get(0))).getString("id"), "comments"); // or other things as following
			jsonArrayList = graph.getSpecificInfo(((JSONObject) (((JSONArray) feed.get("data")).get(0))).getString("id"), "likes"); 
			for(int index = 0; index < jsonArrayList.size(); index ++){
				JSONArray tmp = jsonArrayList.get(index);
				for(int i = 0 ; i<tmp.length(); i++){
					JSONObject json = tmp.getJSONObject(i);
					//TODO here
					// For example 
					Comment comment = new Comment();
					comment.parseJson(json);
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

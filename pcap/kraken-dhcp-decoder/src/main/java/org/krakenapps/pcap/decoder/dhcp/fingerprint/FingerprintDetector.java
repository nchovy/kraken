/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.pcap.decoder.dhcp.fingerprint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class FingerprintDetector {
	//private final Logger logger = LoggerFactory.getLogger(FingerprintDetector.class.getName());
	private static Map<String, FingerprintMetadata> fingerprints = new HashMap<String, FingerprintMetadata>();

	private static final String CATEGORY = "category";
	private static final String VENDOR = "vendor";
	private static final String FAMILY = "family";
	private static final String CRITERION = "criterion";	
	private static final String[] overridables = { CATEGORY, VENDOR, FAMILY };
	
	private static final String ID = "id";
	private static final String DESCRIPTION = "description";
	private static final String FINGERPRINT = "fingerprint";

	static {
		try {
			InputStream is = FingerprintDetector.class.getResourceAsStream("fingerprints.conf");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			JSONTokener jt = new JSONTokener(br);

			Map<String, Map<String, String>> encyclopedia = new HashMap<String, Map<String, String>>();

			try {
				JSONObject config = new JSONObject(jt);
				for(String key : overridables) {
					JSONArray array = config.optJSONArray(key);
					Map<String, String> dictionary = new HashMap<String, String>();
					if (array != null) {		
						int len = array.length();
						for(int i = 0; i < len; ++i) {
							JSONObject obj = array.getJSONObject(i);
							String id = obj.getString(ID);
							String description = obj.getString(DESCRIPTION);
							dictionary.put(id, description);
						}
					}
					encyclopedia.put(key, dictionary);
				}
				
				JSONArray array = config.optJSONArray(CRITERION);
				if (array != null) {
					int len_array = array.length();
					for(int i = 0; i < len_array; ++i) {
						JSONObject object = array.getJSONObject(i);
						FingerprintMetadata fm;
						{
							String category = null, vendor = null, family = null, description = null;							
							for(String key : overridables) {
								String value = object.optString(key);
								Map<String, String> dictionary = encyclopedia.get(key);
								if (dictionary != null) {
									String candidate = dictionary.get(value);
									if (candidate != null)
										value = candidate;
								}
								if(key == CATEGORY)
									category = value;
								else if(key == VENDOR)
									vendor = value;
								else if(key == FAMILY)
									family = value;
							}
							description = object.optString(DESCRIPTION, null);
							
							fm = new FingerprintMetadata(category, vendor, family, description);
						}
						
						JSONArray fingers = object.optJSONArray(FINGERPRINT);
						if (fingers != null) {
							int len_fingers = fingers.length();
							for(int i_fingers = 0; i_fingers < len_fingers; ++i_fingers) {
								String finger = fingers.getString(i_fingers);
								fingerprints.put(finger, fm);
							}
						}
					}
				}
			}
			catch (JSONException e) {
				throw new IOException("cannot parse fingerprints.conf:", e);
			}
		} catch (IOException e) {
			throw new IllegalStateException("cannot load fingerprints:", e);
		}
	}

	public static FingerprintMetadata matches(String fingerprint) {
		if (fingerprint == null)
			return null;
		
		FingerprintMetadata metadata = fingerprints.get(fingerprint);
		return metadata;
	}
}

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
 package org.krakenapps.safebrowsing.google;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.krakenapps.safebrowsing.interfaces.SafeBrowsing;
import org.krakenapps.safebrowsing.util.IOManager;
import org.krakenapps.safebrowsing.util.MD5Hasher;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.annotations.Invalidate;



@Component(name = "google-safe-browsing-api")
@Provides
public class GoogleSafeBrowsing implements SafeBrowsing {

	static final String GSB_UPDATE_KEY = "ABQIAAAAy0sCl79FH-dBBzUR15QbvxSfDyd5lVC1giAQbl8bL8ycMeeP-Q";

	static final String GSB_UPDATE_ROOT		= "http://sb.google.com/safebrowsing/update";
	static final String GSB_UPDATE_QUERY	= "?client=api&apikey=";
	static final String GSB_UPDATE_VERSQ	= "&version=";
	static final String GSB_UPDATE_TYPE_BL	= "goog-black-hash";
	static final String GSB_UPDATE_TYPE_MW	= "goog-malware-hash";
	static final String GSB_UPDATE_ALL_VERSION	= ":1:-1";
	// version usage : "&version=type:prior:minor"

	static String GSB_DATA_ROOT;
	static final String GSB_DATA_MALWARE	= "malware.gsb";
	static final String GSB_DATA_BLACKLIST	= "blacklist.gsb";
	
	List<String> malwareList;
	List<String> blackList;

	public GoogleSafeBrowsing() {
		GSB_DATA_ROOT = "data/safebrowsing/";
		IOManager.PrepareDataPath(GSB_DATA_ROOT);
	}
	public GoogleSafeBrowsing(String path) {
		GSB_DATA_ROOT = path;
		IOManager.PrepareDataPath(GSB_DATA_ROOT);
	}

	@Validate
	@Override
	public void start() {
		IOManager.PrepareDataPath(GSB_DATA_ROOT);
	}

	@Invalidate
	@Override
	public void stop() {
	}

	public void update() {
		updateMalware();
		updateBlacklist();
	}
	
	@Override
	public void updateMalware() {
		// init list
		malwareList = new ArrayList<String>();

		// make data path
		String dataPath = GSB_DATA_ROOT+GSB_DATA_MALWARE;

		// make update url
		String updateURL = GSB_UPDATE_ROOT+GSB_UPDATE_QUERY;
		updateURL += GSB_UPDATE_KEY;
		updateURL += GSB_UPDATE_VERSQ;
		updateURL += GSB_UPDATE_TYPE_MW;

		// update data
		UpdateData(dataPath, updateURL, "GSB-Malware", malwareList);
	}

	@Override
	public void updateBlacklist() {
		// init list
		blackList = new ArrayList<String>();

		// make data path
		String dataPath = GSB_DATA_ROOT+GSB_DATA_BLACKLIST;

		// make update url
		String updateURL = GSB_UPDATE_ROOT+GSB_UPDATE_QUERY;
		updateURL += GSB_UPDATE_KEY;
		updateURL += GSB_UPDATE_VERSQ;
		updateURL += GSB_UPDATE_TYPE_BL;

		// update data
		UpdateData(dataPath, updateURL, "GSB-BlackList", blackList);
	}
	
	private void UpdateData(String path, String url, String tag, List<String> gsbList) {
		// read latest version from web
		Scanner urlScan = IOManager.GetUrlScanner(url+":1:-1");
		if( urlScan == null ) {
			System.out.print(tag+" - Error occured in update : ");
			System.out.println("Cannot access '"+url+"'");
			return;
		}
		String webVersion = urlScan.nextLine();
		urlScan.close();

		// parsing web version
		webVersion = webVersion.split(" ")[1];
		if( webVersion.endsWith("]") )
			webVersion = webVersion.substring(0, webVersion.length()-1);
		System.out.println(tag+" : Latest version - "+webVersion);
		
		// read local data
		String localVersion = "";
		boolean bLocalLatest = false;
		Scanner fileScan = IOManager.GetFileScanner(path);
		if( fileScan != null ) {
			// version check
			localVersion = fileScan.nextLine();
			if( localVersion.equalsIgnoreCase(webVersion) ) {
				System.out.println(tag+" : Current version is latest version.");
				bLocalLatest = true;
			}
			else {
				System.out.println(tag+" : Current version - "+localVersion);
			}

			// load local data
			System.out.println(tag+" : Loading local data...");
			LoadDataFromLocalFile(path, fileScan, tag, gsbList);
			fileScan.close();

			// local latest? return.
			if( bLocalLatest ) {
				System.out.println(tag+" : Load data complete.");
				return;
			}
		}
		else {
			System.out.println(tag+" : Local data not exist.");
			localVersion = "1.-1";
		}

		// make update url with local version
		System.out.println(tag+" : Updating local data...");
		localVersion = ":"+localVersion.replace('.', ':');
		url += localVersion;
		
		// Get update list from WEB
		urlScan = IOManager.GetUrlScanner(url);
		if( urlScan == null ) {
			System.out.print(tag+"- Error occured in update : ");
			System.out.println("Cannot access '"+url+"'");
			return;
		}
		LoadDataFromWeb(path, urlScan, tag, gsbList);
		urlScan.close();
		System.out.println(tag+" : Local data updated.");

		// update local data
		UpdateLocalData(path, webVersion, tag, gsbList);

		System.out.println(tag+" : Load complete.");
	}

	private void LoadDataFromLocalFile(String path, Scanner fileScan, String tag, List<String> gsbList) {
		String line = "";
		int count = 0;
		while( fileScan.hasNext() ) {
			line = fileScan.nextLine();
			gsbList.add(line);
			++count;
		}
		System.out.println(tag+" : Load data complete - #"+count);
		Collections.sort(gsbList);
	}

	private void LoadDataFromWeb(String path, Scanner urlScan, String tag, List<String> gsbList) {
		String line = "";
		int added = 0;
		int deleted = 0;
		while( urlScan.hasNextLine() ) {
			try {
				line = urlScan.nextLine();
				line = line.substring(0,line.lastIndexOf("\t"));
				if( line.charAt(0) == '+' ) {
					line = line.substring(1, line.length());
					int index = Collections.binarySearch(gsbList, line);
					if( index < 0 ) {
						gsbList.add(line);
					}
					++added;
				}
				else if( line.charAt(0) == '-' ) {
					line = line.substring(1, line.length());
					int index = Collections.binarySearch(gsbList, line);
					if( index >= 0 ) {
						gsbList.remove(index);
					}
					++deleted;
				}
			}
			catch( StringIndexOutOfBoundsException e ) {}
		}
		System.out.println(tag+" : Rule added "+added+", deleted "+deleted+".");
		Collections.sort(gsbList);
	}

	private void UpdateLocalData(String path, String version, String tag, List<String> gsbList) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(path));
			pw.println(version);	// write version
			
			for( int i=0; i<gsbList.size(); ++i ) {
				pw.println( gsbList.get(i) );
			}
			pw.close();
			System.out.println(tag+" : Local copy of data updated.");
		}
		catch (IOException e) {
			System.out.println(tag+" : File cannot opened");
		}
	}

	@Override
	public int SafeCheck(String url) {
		String urlHash = MD5Hasher.generateHash(url);
		if( isMalwareListed(urlHash) ) {
			System.out.println("GSB-Malware : malware access! - "+url);
			return 1;
		}
		else if( isBlackListed(urlHash) ) {
			System.out.println("GSB-Blacklist : blacklist access! - "+url);
			return 2;
		}
		return 0;
	}
	public int SafeCheckHash(String urlHash) {
		int ret = 0;
		if( isMalwareListed(urlHash) ) {
			System.out.println("Malware!");
			ret = 1;
		}
		if( isBlackListed(urlHash) ) {
			System.out.println("Malware!");
			ret = 2;
		}
		if( ret == 0 )
			System.out.println("Safe!");
		return ret;
	}
	

	private boolean isBlackListed(String urlHash) {
		if( blackList.isEmpty() )
			return false;
		if( CheckList(blackList, urlHash) >= 0 )
			return true;
		return false;
	}

	private boolean isMalwareListed(String urlHash) {
		if( malwareList.isEmpty() )
			return false;
		if( CheckList(malwareList, urlHash) >= 0 )
			return true;
		return false;
	}

	public int CheckList(List<String> hashList, String urlHash) {
		int result = Collections.binarySearch(hashList, urlHash);
		return result;
	}

	public void test() {
//		List<String> testList = new ArrayList<String>();
//		testList.add("a");		// 0
//		testList.add("aaa");	// 1
//		testList.add("ab");		// 2
//		testList.add("b");
//		testList.add("c");
//		testList.add("d");
//		testList.add("e");
//		testList.add("f");
//		int result = CheckList(testList, "abc");
//		if( result == -1 )
//			System.out.println("result : not found!");
//		else
//			System.out.println("result : index "+result);
	}

}

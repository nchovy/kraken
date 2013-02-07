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
 package org.krakenapps.sonar.passive.safebrowsing.util;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class IOManager {
	public static void PrepareDataPath(String path) {
		File dir = new File(path);
		if(!dir.isDirectory()) {
			dir.mkdirs();
		}
	}
	public static Scanner GetUrlScanner(String url) {
		Scanner s = null;
		try {
			URLConnection con = (new URL(url)).openConnection();
			s = new Scanner(con.getInputStream());
		}
		catch(Exception e) {	e.printStackTrace();	}
		return s;
	}
	public static Scanner GetFileScanner(String file) {
		Scanner s = null;
		try {
			s = new Scanner(new File(file));
		}
		catch(Exception e) {}
		return s;
	}

}

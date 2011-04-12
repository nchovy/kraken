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

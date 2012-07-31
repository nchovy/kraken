package org.krakenapps.docxcod.test;

import java.io.File;
import java.util.ArrayList;

public class TearDownHelper {
	public ArrayList<File> outputFiles = new ArrayList<File>();

	public void tearDown() {
		for (File f : outputFiles) {
			try {
				if (f.isDirectory())
					deleteDir(f);
				else
					f.delete();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	public static void deleteDir(File file) {
		if (file.isDirectory()) {
			if (file.listFiles().length != 0) {
				File[] fileList = file.listFiles();
				for (int i = 0; i < fileList.length; i++) {
					deleteDir(fileList[i]);
					file.delete();
				}
			} else {
				file.delete();
			}
		} else {
			file.delete();
		}
	}

	public void add(File file) {
		outputFiles.add(file);
	}

}

package org.krakenapps.docxcod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {

	public static void saveReport(RptOutput mergedOutput, File outputFile) throws FileNotFoundException {
		FileOutputStream rptOutputStream = null;
		InputStream rptInputStream = null;
		try {
			rptOutputStream = new FileOutputStream(outputFile);
			rptInputStream = mergedOutput.createInputStream();
			copyStream(rptInputStream, rptOutputStream);
		} finally {
			closeStream(rptOutputStream);
			closeStream(rptInputStream);
		}
	}

	private static void copyStream(InputStream inputStream, OutputStream rptOutputStream) {
		byte[] buf = new byte[8192];
		int readCnt = 0;
		try {
			do {
				readCnt = inputStream.read(buf);
				if (readCnt == -1)
					break;
				else {
					rptOutputStream.write(buf, 0, readCnt);
				}
			} while (readCnt != 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void closeStream(InputStream is) {
		try {
			if (is != null)
				is.close();
		} catch (IOException e) {
			// ignore
		}
	}

	private static void closeStream(OutputStream os) {
		try {
			if (os != null)
				os.close();
		} catch (IOException e) {
			// ignore
		}
	}

}

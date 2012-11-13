package org.krakenapps.docxcod.util;

import java.io.Closeable;
import java.util.Scanner;

public class CloseableHelper {
	public static void safeClose(Closeable c) {
		if (c == null)
			return;
		try {
			c.close();
		} catch (Exception e) {
			// ignore
		}
	}
	
	public static void safeClose(Scanner c) {
		if (c == null)
			return;
		try {
			c.close();
		} catch (Exception e) {
			// ignore
		}		
	}	
}

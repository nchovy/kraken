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
package org.krakenapps.util.directoryfile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.krakenapps.util.directoryfile.DirectoryFileArchive.IndexEntry;
import org.krakenapps.util.directoryfile.DirectoryFileArchive.IndexEntryType;

public class Utility {
	public static void main(String[] args) {
		if (args.length < 1) {
			printUsage();
			return;
		}

		String method = args[0];

		try {
			Method declaredMethod = Utility.class.getDeclaredMethod(method, String[].class);
			declaredMethod.setAccessible(true);
			Object ret = declaredMethod.invoke(null, new Object[] { args });
			System.out.println("retval: " + ret);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			printUsage();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			printUsage();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			printUsage();
		} catch (SecurityException e) {
			e.printStackTrace();
			printUsage();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			printUsage();
		}
	}

	public static void extractFiles(String[] args) {
		if (args.length < 2 || "-h".equals(args[1]) || "--help".equals(args[1])) {
			System.out.println("Usage: extractFiles jdf targetdir");
			return;
		}

		String jdfFilePath = args[1];
		String targetDir = ".";
		if (args.length > 2) {
			targetDir = args[2];
		}

		if (!new File(targetDir).exists()) {
			System.err.println(String.format("Target directory is not found: %s", targetDir));
			return;
		}

		File jdfFile = new File(jdfFilePath);
		if (!jdfFile.exists()) {
			System.err.println(String.format("JDF file is not found: %s", jdfFile.getAbsolutePath()));
			return;
		}
		try {
			_extractFiles(jdfFile, targetDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void _extractFiles(File jdfFile, String targetDir) throws IOException {
		String dirAbsPath = jdfFile.getParentFile().getAbsolutePath();
		ConcurrentSkipListMap<String, IndexEntry> index = new ConcurrentSkipListMap<String, DirectoryFileArchive.IndexEntry>();
		DirectoryFileArchive.loadIndex(dirAbsPath, index);
		RandomAccessFile jdfRaFile = new RandomAccessFile(jdfFile, "r");

		long splitFileCount = 0;
		String splitFileSubPath = null; 
		for (Map.Entry<String, IndexEntry> e : index.entrySet()) {
			String subPath = e.getKey();
			IndexEntry indexEntry = e.getValue();

			if (indexEntry.type == IndexEntryType.DIRECTORY)
				continue;

			
			if (indexEntry.reserved != 0) {
				splitFileCount = indexEntry.reserved + 1;
				splitFileSubPath = subPath;
			}
			File f = null;
			if (splitFileCount > 0) {
				f = new File(targetDir, splitFileSubPath);
			} else {
				f = new File(targetDir, subPath);
				System.out.println(subPath);
			}
			f.getParentFile().mkdirs();
			FileOutputStream fos = null;
			try {

				MappedByteBuffer sourceBuffer = jdfRaFile.getChannel().map(MapMode.READ_ONLY, indexEntry.startPos,
						indexEntry.size);
				if (splitFileCount > 0) 
					fos = new FileOutputStream(new File(targetDir, splitFileSubPath), true);
				else 
					fos = new FileOutputStream(new File(targetDir, subPath));
					
				sourceBuffer.load();
				byte[] b = new byte[4096];
				while(sourceBuffer.hasRemaining()) {
					int reqSize = sourceBuffer.remaining() > 4096 ? 4096 : sourceBuffer.remaining();
					sourceBuffer.get(b, 0, reqSize);
					fos.write(b, 0, reqSize);
				}
				
				if (splitFileCount > 0) {
					splitFileCount --;
				}				

			} catch (IOException ex) {
				System.err.println(String.format("Exception occured for %s: %s", subPath, ex.getMessage()));
				continue;
			} finally {
				if (fos != null)
					fos.close();
			}

		}
	}

	private static void printUsage() {
		System.out.println("supported methods:");
		Method[] declaredMethods = Utility.class.getDeclaredMethods();
		for (Method m : declaredMethods) {
			if ("main".equals(m.getName()))
				continue;
			if (!Arrays.equals(m.getParameterTypes(), new Class<?>[] { String[].class }))
				continue;
			System.out.println("\t" + m.getName());
		}
	}
}

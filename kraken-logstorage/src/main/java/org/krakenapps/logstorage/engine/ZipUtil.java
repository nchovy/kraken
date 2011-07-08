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
package org.krakenapps.logstorage.engine;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

// unused
public class ZipUtil {
	public static void zip(File input, File output) throws FileNotFoundException, IOException {
		byte[] block = new byte[16384];

		FileInputStream fis = null;
		FileOutputStream fos = null;
		ZipOutputStream zos = null;

		try {
			fis = new FileInputStream(input);
			fos = new FileOutputStream(output);
			zos = new ZipOutputStream(fos);
			zos.setLevel(8);

			ZipEntry entry = new ZipEntry(input.getName());
			entry.setTime(input.lastModified());
			zos.putNextEntry(entry);

			int readBytes = 0;
			while ((readBytes = fis.read(block)) != -1) {
				zos.write(block, 0, readBytes);
			}

			zos.closeEntry();

		} finally {
			if (zos != null)
				zos.close();
			if (fis != null)
				fis.close();
			if (fos != null)
				fos.close();
		}
	}

	public static void unzip(File input) throws IOException {
		FileInputStream fis = null;
		ZipInputStream zis = null;
		ZipEntry entry = null;

		try {
			fis = new FileInputStream(input);
			zis = new ZipInputStream(fis);

			while ((entry = zis.getNextEntry()) != null) {
				File targetFile = new File(entry.getName());
				unzipEntry(zis, targetFile);
			}
		} finally {
			if (zis != null)
				zis.close();

			if (fis != null)
				fis.close();
		}
	}

	private static void unzipEntry(ZipInputStream zis, File out) throws IOException {
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(out);
			bos = new BufferedOutputStream(fos);

			byte[] buffer = new byte[16384];
			int readBytes = 0;
			while ((readBytes = zis.read(buffer)) != -1)
				bos.write(buffer, 0, readBytes);

		} catch (FileNotFoundException e) {
		} finally {
			if (bos != null)
				bos.close();

			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
				}
		}
	}

}

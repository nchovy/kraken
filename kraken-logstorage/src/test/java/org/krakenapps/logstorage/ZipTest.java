/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.logstorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.junit.Test;
import org.krakenapps.logstorage.engine.ZipFileReader;
import org.krakenapps.logstorage.engine.ZipFileWriter;

public class ZipTest {
	// @Test
	public void unzip() throws IOException {
		File decomp = new File("decomp.log");
		decomp.delete();

		long begin = new Date().getTime();
		ZipFileReader reader = new ZipFileReader(new File("comp.log"),
				640 * 1024);

		FileOutputStream os = new FileOutputStream(decomp);
		byte[] b = new byte[20000];
		int len;

		while (true) {
			len = reader.read(b);
			if (len <= 0)
				break;

			os.write(b, 0, len);
		}

		reader.close();
		os.close();
		long end = new Date().getTime();

		System.out.println("read time: " + (end - begin));
	}

	// @Test
	public void zip() throws IOException {
		File comp = new File("comp.log");
		comp.delete();

		long begin = new Date().getTime();

		FileInputStream is = new FileInputStream("nchovy.kr-access_log");
		ZipFileWriter writer = new ZipFileWriter(comp, 640 * 1024);
		// FileOutputStream writer = new FileOutputStream(comp);

		byte[] b = new byte[8096];
		while (is.available() > 0) {
			int len = is.read(b);
			writer.append(b, 0, len);
			// writer.write(b, 0, len);
		}

		is.close();
		writer.close();
		long end = new Date().getTime();

		System.out.println(end - begin);
	}
}

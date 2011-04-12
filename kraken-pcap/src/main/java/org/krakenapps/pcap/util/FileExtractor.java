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
package org.krakenapps.pcap.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Extracts file from input stream.
 * 
 * @author mindori
 */
public class FileExtractor {
	private FileExtractor() {
	}

	public static void extract(File file, InputStream is) throws IOException {
		FileOutputStream fs = new FileOutputStream(file);
		byte[] buffer = new byte[8192];
		while (true) {
			int length = is.read(buffer);
			if (length < 0)
				break;

			fs.write(buffer, 0, length);
		}

		fs.close();
	}
}

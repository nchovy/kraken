/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.eventstorage.engine;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.krakenapps.api.DateFormat;

public class DatapathUtil {
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	public static enum FileType {
		Index("idx"), Pointer("ptr"), Data("dat");

		private static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		private String ext;

		private FileType(String ext) {
			this.ext = ext;
		}

		public static boolean isValidFilename(String filename) {
			if (filename.endsWith(Index.toString()) || filename.endsWith(Data.toString())) {
				if (filename.length() != 14)
					return false;

				try {
					sdf.parse(filename.substring(0, 10));
				} catch (ParseException e) {
					return false;
				}

				return true;
			} else if (filename.endsWith(Pointer.toString())) {
				if (!filename.startsWith("data"))
					return false;

				try {
					Integer.parseInt(filename.substring(4, filename.length() - 4));
				} catch (NumberFormatException e) {
					return false;
				}

				return true;
			}

			return false;
		}

		@Override
		public String toString() {
			return "." + ext;
		}
	}

	private static File logDir;

	public static void setLogDir(File logDir) {
		DatapathUtil.logDir = logDir;
	}

	public static File getDirPath(int tableId) {
		File dir = new File(logDir, Integer.toString(tableId));
		if (dir.exists() && !dir.isDirectory())
			throw new IllegalStateException(dir.getAbsolutePath() + " is not directory");
		return dir;
	}

	public static List<Date> getLogDates(int tableId) {
		final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		final List<Date> dates = new ArrayList<Date>();

		getDirPath(tableId).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.length() != 14)
					return false;
				if (!name.endsWith(FileType.Index.toString()))
					return false;

				try {
					Date date = sdf.parse(name.substring(0, 10));
					dates.add(date);
				} catch (ParseException e) {
					return false;
				}
				return true;
			}
		});
		Collections.sort(dates, Collections.reverseOrder());

		return dates;
	}

	public static File getFilePath(int tableId, Date day, FileType type) {
		File tableDir = getDirPath(tableId);
		if (!tableDir.exists())
			tableDir.mkdirs();
		String dateText = DateFormat.format(DATE_FORMAT, day);
		return new File(tableDir, dateText + type.toString());
	}
}

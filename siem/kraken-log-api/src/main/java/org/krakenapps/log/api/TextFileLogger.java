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
package org.krakenapps.log.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFileLogger extends AbstractLogger {
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TextFileLogger.class.getName());
	private RotatingLogFileReader reader;
	private DateParser dateParser;

	public TextFileLogger(LoggerSpecification spec, LoggerFactory loggerFactory) throws FileNotFoundException,
			IOException {
		super("local", spec.getName(), spec.getDescription(), loggerFactory, spec.getLogCount(), spec.getLastLogDate(),
				spec.getConfig());

		Properties config = spec.getConfig();
		String filePath = config.getProperty("file.path");
		String datePattern = config.getProperty("date.pattern");
		if (datePattern == null)
			datePattern = "MMM dd HH:mm:ss";

		String dateExtractor = config.getProperty("date.extractor");
		if (dateExtractor == null || dateExtractor.isEmpty())
			dateExtractor = dateFormatToRegex(datePattern);

		String dateLocale = config.getProperty("date.locale");
		if (dateLocale == null)
			dateLocale = "en";

		this.reader = new RotatingLogFileReader(filePath);

		String offset = config.getProperty("last_offset");
		String firstLine = config.getProperty("first_line");

		logger.trace("kraken log api: text logger [{}] last offset [{}], last line [{}]", new Object[] {
				spec.getName(), offset, firstLine });

		reader.setFirstLine(firstLine);
		reader.setLastOffset(offset == null ? 0 : Long.valueOf(offset));

		this.dateParser = new DefaultDateParser(new SimpleDateFormat(datePattern, new Locale(dateLocale)),
				dateExtractor);
	}

	private String dateFormatToRegex(String pattern) {
		StringBuilder regex = new StringBuilder();
		boolean isInQuote = false;
		int l = pattern.length();

		regex.append("(");

		for (int i = 0; i < l; i++) {
			if (i + 1 < l && pattern.charAt(i) == '\'') {
				if (pattern.charAt(i + 1) == '\'') {
					regex.append("'");
					i++;
				} else {
					if (isInQuote) {
						if (pattern.charAt(i) == '\'') {
							isInQuote = false;
							continue;
						}
						regex.append(pattern.charAt(i));
						continue;
					} else
						isInQuote = true;
				}
				continue;
			}

			int r = 1;
			while (i + 1 < l && pattern.charAt(i) == pattern.charAt(i + 1)) {
				r++;
				i++;
				continue;
			}

			switch (pattern.charAt(i)) {
			case 'G':
				regex.append("(AD|BC)");
				break;

			case 'W':
			case 'F':
				regex.append("\\d" + repeat(1, r));
				break;

			case 'E':
				if (r <= 3)
					regex.append(".{3}");
				else
					regex.append("\\p{Upper}\\p{Lower}+day");
				break;

			case 'a':
				regex.append("(AM|PM)");
				break;

			case 'M':
				if (r > 3) {
					regex.append("(?i)(January|February|March|April|May|June|July|August|September|"
							+ "October|November|December|Undecimber)");
					break;
				} else if (r == 3) {
					regex.append(".{3}");
					break;
				}
			case 'w':
			case 'd':
			case 'H':
			case 'k':
			case 'K':
			case 'h':
			case 'm':
			case 's':
				regex.append("\\d" + repeat(Math.max(1, r), Math.max(2, r)));
				break;

			case 'D':
				regex.append("\\d" + repeat(Math.max(1, r), Math.max(3, r)));
				break;

			case 'y':
				regex.append("\\d" + repeat(Math.max(1, r), Math.max(2, r)));
				break;

			case 'S':
				regex.append("\\d" + repeat(Math.max(1, r), Math.max(3, r)));
				break;

			case 'Z':
				regex.append("[+-]\\d" + repeat(4));
				break;

			case '(':
			case ')':
			case '{':
			case '}':
				regex.append("\\");
			default:
				regex.append(pattern.charAt(i));
				if (r > 1)
					regex.append(repeat(r));
			}
		}
		regex.append(")");

		return regex.toString();
	}

	private String repeat(int num) {
		return "{" + num + "}";
	}

	private String repeat(int min, int max) {
		if (min == max)
			return repeat(min);
		return "{" + min + "," + max + "}";
	}

	@Override
	protected void runOnce() {
		try {
			this.reader.open();
			while (true) {
				if (getStatus() == LoggerStatus.Stopping || getStatus() == LoggerStatus.Stopped)
					break;

				String line = reader.readLine();
				if (line == null)
					break;

				if (line.isEmpty())
					continue;

				if (logger.isDebugEnabled())
					logger.debug("kraken log api: text logger [{}], read line [{}]", getFullName(), line);

				Date date = dateParser.parse(line);
				if (date == null) {
					logger.trace("kraken log api: cannot parse date [{}]", line);
					return;
				}

				Map<String, Object> params = new HashMap<String, Object>();
				params.put("date", date);
				params.put("line", line);

				Log log = new SimpleLog(date, getFullName(), params);
				write(log);
			}

			getConfig().put("first_line", reader.getFirstLine());
			getConfig().put("last_offset", reader.getLastOffset());

			logger.trace("kraken log api: name [{}], updated offset [{}]", getName(), reader.getLastOffset());
		} catch (Exception e) {
			logger.error("kraken log api: cannot read log file", e);
		} finally {
			this.reader.close();
		}
	}

	private class DefaultDateParser implements DateParser {
		private SimpleDateFormat dateFormat;
		private String dateExtractor;

		public DefaultDateParser(SimpleDateFormat dateFormat, String dateExtractor) {
			this.dateFormat = dateFormat;
			this.dateExtractor = dateExtractor;
		}

		@Override
		public Date parse(String line) {
			Pattern p = Pattern.compile(dateExtractor);
			Matcher m = p.matcher(line);

			if (!m.find() || m.groupCount() == 0) {
				logger.error("kraken log api: cannot find date extractor pattern in log file, " + reader.getFilePath());
				return null;
			}

			do {
				for (int group = 1; group <= m.groupCount(); group++) {
					try {
						String dateString = m.group(group);
						Date date = dateFormat.parse(dateString);
						Calendar c = Calendar.getInstance();
						int currentYear = c.get(Calendar.YEAR);
						c.setTime(date);

						int year = c.get(Calendar.YEAR);
						if (year == 1970)
							c.set(Calendar.YEAR, currentYear);

						return c.getTime();
					} catch (ParseException e) {
					}
				}
			} while (m.find());

			logger.error("kraken log api: cannot find date in log file, " + reader.getFilePath());
			return null;
		}
	}
}

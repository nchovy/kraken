package org.krakenapps.logdb.query.command;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.LogParser;
import org.krakenapps.logdb.LogQueryCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextFile extends LogQueryCommand {
	private final Logger logger = LoggerFactory.getLogger(TextFile.class.getName());
	private FileInputStream is;
	private LogParser parser;
	private int offset;
	private int limit;

	public TextFile(FileInputStream is, LogParser parser, int offset, int limit) {
		this.is = is;
		this.parser = parser;
		this.offset = offset;
		this.limit = limit;
	}

	@Override
	public void start() {
		status = Status.Running;

		BufferedReader br = null;
		try {
			Charset utf8 = Charset.forName("utf-8");
			br = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), utf8));

			int i = 0;
			int count = 0;
			while (true) {
				if (limit > 0 && count >= limit)
					break;

				String line = br.readLine();
				if (line == null)
					break;

				Map<String, Object> m = new HashMap<String, Object>();
				Map<String, Object> parsed = null;
				m.put("line", line);
				if (parser != null) {
					parsed = parser.parse(m);
					if (parsed == null)
						continue;
				}

				if (i >= offset) {
					write(new LogMap(parsed != null ? parsed : m));
					count++;
				}
				i++;
			}
		} catch (Throwable t) {
			logger.error("kraken logdb: file error", t);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}

		eof();
	}

	@Override
	public void push(LogMap m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isReducer() {
		return false;
	}

}

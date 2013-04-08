package org.krakenapps.logdb.query.command;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.krakenapps.logdb.LogQueryCommand;

public class OutputCsv extends LogQueryCommand {
	private Charset utf8;
	private List<String> fields;
	private FileOutputStream os;

	public OutputCsv(FileOutputStream os, List<String> fields) {
		this.os = os;
		this.fields = fields;
		this.utf8 = Charset.forName("utf-8");

		// write first header line
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String field : fields) {
			if (i != 0)
				sb.append(",");
			sb.append(field);
			i++;
		}
		sb.append("\n");

		try {
			os.write(sb.toString().getBytes(utf8));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void push(LogMap m) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String field : fields) {
			if (i != 0)
				sb.append(",");

			Object value = m.get(field);
			if (value != null)
				sb.append(value);
			i++;
		}
		sb.append("\n");

		try {
			os.write(sb.toString().getBytes(utf8));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean isReducer() {
		return false;
	}

	@Override
	public void eof() {
		this.status = Status.Finalizing; 
		try {
			os.close();
		} catch (IOException e) {
		}
		super.eof();
	}
}

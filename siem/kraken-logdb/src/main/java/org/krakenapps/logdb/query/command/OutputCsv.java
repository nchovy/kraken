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
package org.krakenapps.logdb.query.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.krakenapps.logdb.LogQueryCommand;

public class OutputCsv extends LogQueryCommand {
	private Charset utf8;
	private List<String> fields;
	private File f;
	private FileOutputStream os;

	public OutputCsv(File f, List<String> fields) throws IOException {
		this.f = f;
		this.os = new FileOutputStream(f);
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

	public File getCsvFile() {
		return f;
	}

	public List<String> getFields() {
		return fields;
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

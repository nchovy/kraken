package org.krakenapps.logstorage.query.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.krakenapps.logstorage.LogQueryCallback;
import org.krakenapps.logstorage.LogQueryCommand;
import org.krakenapps.logstorage.query.FileBufferList;

public class Result extends LogQueryCommand {
	private FileBufferList<Map<String, Object>> result;
	private List<LogQueryCallback> callbacks;

	public Result() throws IOException {
		this(new ArrayList<LogQueryCallback>());
	}

	public Result(List<LogQueryCallback> callbacks) throws IOException {
		this.result = new FileBufferList<Map<String, Object>>();
		this.callbacks = callbacks;
	}

	@Override
	public void push(Map<String, Object> m) {
		String str = (String) m.get("_data");
		if (str != null) {
			List<String> data = new ArrayList<String>();
			int l = 0;
			int r;
			while ((r = str.indexOf(' ', l)) != -1) {
				data.add(str.substring(l, r));
				l = r + 1;
			}
			data.add(str.substring(l));

			if (data.size() == header.length) {
				m.remove("_data");
				for (int i = 0; i < header.length; i++) {
					if (!m.containsKey(header[i]))
						m.put(header[i], data.get(i));
				}
			}
		}

		result.add(m);
	}

	public FileBufferList<Map<String, Object>> getResult() {
		return result;
	}

	public List<Map<String, Object>> getResult(int offset, int limit) {
		List<Map<String, Object>> r = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < limit; i++) {
			if (offset + i >= result.size())
				break;
			r.add(result.get(offset + i));
		}
		return r;
	}

	@Override
	public void eof() {
		for (LogQueryCallback callback : callbacks)
			callback.callback(result);
		super.eof();
	}
}

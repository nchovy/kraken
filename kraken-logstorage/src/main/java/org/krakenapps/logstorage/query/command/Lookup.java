package org.krakenapps.logstorage.query.command;

import java.util.Map;

import org.krakenapps.logstorage.LogQueryCommand;
import org.krakenapps.logstorage.LogQueryService;
import org.krakenapps.logstorage.LookupHandler;

public class Lookup extends LogQueryCommand {
	private LookupHandler handler;
	private String tableName;
	private String srcField;
	private String localSrcField;
	private String dstField;
	private String localDstField;

	public Lookup(String tableName, String srcField, String dstField) {
		this(tableName, srcField, srcField, dstField, dstField);
	}

	public Lookup(String tableName, String srcField, String localSrcField, String dstField, String localDstField) {
		this.tableName = tableName;
		this.srcField = srcField;
		this.localSrcField = localSrcField;
		this.dstField = dstField;
		this.localDstField = localDstField;
	}

	public void setLogQueryService(LogQueryService service) {
		this.handler = service.getLookupHandler(tableName);
	}

	@Override
	public void push(Map<String, Object> m) {
		Object value = getData(localSrcField, m);
		if (handler != null)
			m.put(localDstField, handler.lookup(srcField, dstField, value));
		write(m);
	}
}

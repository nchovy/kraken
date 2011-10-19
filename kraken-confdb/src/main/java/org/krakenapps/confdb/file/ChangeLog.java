package org.krakenapps.confdb.file;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CommitLog;

/**
 * database level change set log
 * 
 * @author xeraph
 * 
 */
class ChangeLog implements CommitLog {
	private String committer;

	private String message;

	private List<ConfigChange> changeset = new ArrayList<ConfigChange>();

	@Override
	public int getRev() {
		// TODO:
		return 0;
	}

	@Override
	public Date getCreated() {
		// TODO:
		return new Date();
	}

	@Override
	public String getCommitter() {
		return committer;
	}

	public void setCommitter(String committer) {
		this.committer = committer;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<ConfigChange> getChangeset() {
		return changeset;
	}

	public void setChangeset(List<ConfigChange> changeset) {
		this.changeset = changeset;
	}

	public byte[] serialize() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("committer", committer);
		m.put("msg", message);
		m.put("changeset", null);

		ByteBuffer bb = ByteBuffer.allocate(EncodingRule.lengthOf(m));
		EncodingRule.encode(bb, m);
		return bb.array();
	}

	public static ChangeLog deserialize(byte[] b) {
		ByteBuffer bb = ByteBuffer.wrap(b);
		Map<String, Object> m = EncodingRule.decodeMap(bb);

		ChangeLog c = new ChangeLog();
		c.setCommitter((String) m.get("committer"));
		c.setMessage((String) m.get("msg"));
		// TODO:
		c.setChangeset(null);
		return c;
	}
}

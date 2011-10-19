package org.krakenapps.confdb.file;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CommitLog;
import org.krakenapps.confdb.ConfigChange;

/**
 * database level change set log
 * 
 * @author xeraph
 * 
 */
class ChangeLog implements CommitLog {
	private long rev;

	private Date created = new Date();

	private String committer;

	private String message;

	@CollectionTypeHint(ConfigChange.class)
	private List<ConfigChange> changeset = new ArrayList<ConfigChange>();

	@Override
	public long getRev() {
		return rev;
	}

	public void setRev(long rev) {
		this.rev = rev;
	}

	@Override
	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
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

	@Override
	public List<ConfigChange> getChangeSet() {
		return changeset;
	}

	public void setChangeSet(List<ConfigChange> changeset) {
		this.changeset = changeset;
	}

	public byte[] serialize() {
		Map<String, Object> m = new HashMap<String, Object>();
		// "rev" is doc id of revision log (do not require serialize)
		m.put("created", created);
		m.put("committer", committer);
		m.put("msg", message);
		m.put("changeset", PrimitiveConverter.serialize(changeset));

		ByteBuffer bb = ByteBuffer.allocate(EncodingRule.lengthOf(m));
		EncodingRule.encode(bb, m);
		return bb.array();
	}

	public static ChangeLog deserialize(byte[] b) {
		ByteBuffer bb = ByteBuffer.wrap(b);
		Map<String, Object> m = EncodingRule.decodeMap(bb);

		ChangeLog c = new ChangeLog();
		c.setCreated((Date) m.get("created"));
		c.setCommitter((String) m.get("committer"));
		c.setMessage((String) m.get("msg"));
		List<Object> list = Arrays.asList((Object[]) m.get("changeset"));
		c.setChangeSet(PrimitiveConverter.parse(ConfigChange.class, list));
		return c;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (ConfigChange c : changeset) {
			if (i++ != 0)
				sb.append(", ");
			sb.append(c.toString());
		}

		return "committer=" + committer + ", msg=" + message + ", changeset=[" + sb.toString() + "]";
	}
}

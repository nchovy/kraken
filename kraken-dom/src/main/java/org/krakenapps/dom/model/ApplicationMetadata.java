package org.krakenapps.dom.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.krakenapps.msgbus.Marshalable;

@Entity
@Table(name = "dom_app_metadatas")
public class ApplicationMetadata implements Marshalable {
	@EmbeddedId
	private ApplicationMetadataKey key;

	private String value;

	@Column(name = "updated_at", nullable = false)
	private Date updateDateTime;

	public ApplicationMetadataKey getKey() {
		return key;
	}

	public void setKey(ApplicationMetadataKey key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("app_guid", key.getApplication().getGuid());
		m.put("name", key.getName());
		m.put("value", value);
		return m;
	}
}

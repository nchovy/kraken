package org.krakenapps.logdb.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.logdb.DataSource;
import org.krakenapps.logdb.DataSourceEventListener;
import org.krakenapps.logdb.DataSourceRegistry;
import org.krakenapps.logstorage.LogTableEventListener;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logstorage.TableMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "data-source-registry")
@Provides
public class DataSourceRegistryImpl implements DataSourceRegistry, LogTableEventListener {
	private final Logger logger = LoggerFactory.getLogger(DataSourceRegistryImpl.class.getName());

	@Requires
	private LogTableRegistry tableRegistry;

	private ConcurrentMap<DataSourceKey, DataSource> sources;
	private CopyOnWriteArraySet<DataSourceEventListener> callbacks;

	@Validate
	public void start() {
		sources = new ConcurrentHashMap<DataSourceKey, DataSource>();
		callbacks = new CopyOnWriteArraySet<DataSourceEventListener>();

		tableRegistry.addListener(this);

		// add initial state
		for (String name : tableRegistry.getTableNames()) {
			int id = tableRegistry.getTableId(name);
			TableMetadata metadata = tableRegistry.getTableMetadata(id);
			update(new LogTableDataSource(name, toMap(metadata)));
		}
	}

	private Map<String, String> toMap(TableMetadata metadata) {
		Map<String, String> m = new HashMap<String, String>();
		for (String key : metadata.keySet()) {
			String value = metadata.get(key);
			m.put(key.toString(), value == null ? null : value.toString());
		}

		return m;
	}

	@Invalidate
	public void stop() {
		if (tableRegistry != null)
			tableRegistry.removeListener(this);
	}

	@Override
	public Collection<DataSource> getAll() {
		return Collections.unmodifiableCollection(sources.values());
	}

	@Override
	public DataSource get(String nodeGuid, String name) {
		return sources.get(new DataSourceKey(nodeGuid, name));
	}

	@Override
	public void update(DataSource ds) {
		sources.put(new DataSourceKey(ds), ds);

		for (DataSourceEventListener callback : callbacks) {
			try {
				callback.onUpdate(ds);
			} catch (Exception e) {
				logger.warn("kraken logdb: data source callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void remove(DataSource ds) {
		sources.remove(new DataSourceKey(ds));

		for (DataSourceEventListener callback : callbacks) {
			try {
				callback.onRemove(ds);
			} catch (Exception e) {
				logger.warn("kraken logdb: data source callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void addListener(DataSourceEventListener listener) {
		callbacks.add(listener);
	}

	@Override
	public void removeListener(DataSourceEventListener listener) {
		callbacks.remove(listener);
	}

	@Override
	public void onCreate(String tableName, Map<String, String> tableMetadata) {
		update(new LogTableDataSource(tableName, tableMetadata));
	}

	@Override
	public void onDrop(String tableName) {
		remove(new LogTableDataSource(tableName));
	}

	private static class LogTableDataSource implements DataSource {
		private String tableName;
		private Map<String, Object> metadata;

		public LogTableDataSource(String tableName) {
			this(tableName, null);
		}

		public LogTableDataSource(String tableName, Map<String, String> metadata) {
			this.tableName = tableName;
			this.metadata = new HashMap<String, Object>();

			if (metadata != null)
				this.metadata = new HashMap<String, Object>(metadata);
		}

		@Override
		public String getNodeGuid() {
			return "local";
		}

		@Override
		public String getType() {
			return "table";
		}

		@Override
		public String getName() {
			return tableName;
		}

		@Override
		public Map<String, Object> getMetadata() {
			return metadata;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LogTableDataSource other = (LogTableDataSource) obj;
			if (tableName == null) {
				if (other.tableName != null)
					return false;
			} else if (!tableName.equals(other.tableName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "table: " + tableName;
		}

	}

	private static class DataSourceKey {
		private String nodeGuid;
		private String name;

		public DataSourceKey(DataSource ds) {
			this(ds.getNodeGuid(), ds.getName());
		}

		public DataSourceKey(String nodeGuid, String name) {
			this.nodeGuid = nodeGuid;
			this.name = name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((nodeGuid == null) ? 0 : nodeGuid.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DataSourceKey other = (DataSourceKey) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (nodeGuid == null) {
				if (other.nodeGuid != null)
					return false;
			} else if (!nodeGuid.equals(other.nodeGuid))
				return false;
			return true;
		}
	}
}

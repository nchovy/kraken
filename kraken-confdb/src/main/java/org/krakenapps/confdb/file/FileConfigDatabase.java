package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileConfigDatabase implements ConfigDatabase {
	private final Logger logger = LoggerFactory.getLogger(FileConfigDatabase.class.getName());

	private File baseDir;
	private String name;
	
	private AtomicLong revCounter;

	private CollectionLogWriter writer;

	private Map<Integer, CollectionMetadata> colMetadatas;
	private ConcurrentMap<Integer, FileConfigCollection> collections;

	public FileConfigDatabase(File baseDir, String name) {
		this.baseDir = baseDir;
		this.name = name;
		
		// TODO: load last revision 
		this.revCounter = new AtomicLong();
		
		this.colMetadatas = new HashMap<Integer, CollectionMetadata>();
		this.collections = new ConcurrentHashMap<Integer, FileConfigCollection>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ConfigCollection ensureCollection(String name) {
		try {
			CollectionMetadata meta = getCollectionMetadata(name);

			// create new collection if not exists
			if (meta == null)
				meta = createCollection(name);

			// is collection already loaded?
			FileConfigCollection col = new FileConfigCollection(this, meta);

			// TODO: collection loading management
			return col;
		} catch (IOException e) {
			logger.error("kraken confdb: cannot open collection file", e);
			return null;
		}
	}

	private CollectionMetadata createCollection(String name) {
		// TODO: add collection id generator
		return new CollectionMetadata(1, name);
	}

	@Override
	public void dropCollection(String name) {

	}

	public File getDbDirectory() {
		return new File(baseDir, name);
	}

	public CollectionMetadata getCollectionMetadata(String name) {
		for (int id : colMetadatas.keySet()) {
			CollectionMetadata meta = colMetadatas.get(id);
			if (meta.getName().equals(name))
				return meta;
		}

		return null;
	}

	public CollectionLogWriter getWriter() {
		return writer;
	}

	public String getCollectionName(int id) {
		if (colMetadatas.containsKey(id))
			return colMetadatas.get(id).getName();

		return null;
	}
	
	public long getNextRevision() {
		return revCounter.incrementAndGet();
	}

}

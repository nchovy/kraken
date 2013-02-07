/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.confdb.file;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

import org.krakenapps.codec.CustomCodec;
import org.krakenapps.codec.UnsupportedTypeException;
import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.ConfigEntry;

public class FileManifestCodec implements CustomCodec {

	@Override
	public void encode(ByteBuffer bb, Object value) {
		if (!(value instanceof FileManifest))
			throw new UnsupportedTypeException(value.toString());

		FileManifest manifest = (FileManifest) value;

		// type byte
		bb.put((byte) 200);
		
		// version 2
		bb.put((byte) 2);
		
		// collection count
		bb.putShort((short) manifest.getCollectionNames().size());

		try {
			for (String colName : manifest.getCollectionNames()) {
				CollectionEntry colEntry = manifest.getCollectionEntry(colName);
				byte[] nameBytes = colName.getBytes("utf-8");
				bb.putShort((short) colEntry.getId());
				bb.putShort((short) nameBytes.length);
				bb.put(nameBytes);

				List<ConfigEntry> configs = manifest.getConfigEntries(colName);
				bb.putShort((short) configs.size());

				for (ConfigEntry config : configs) {
					bb.putInt(config.getDocId());
					bb.putLong(config.getRev());
					bb.putInt(config.getIndex());
				}
			}
		} catch (UnsupportedEncodingException e) {
		}
	}

	@Override
	public Object decode(ByteBuffer bb) {
		byte type = bb.get();
		if (type != (byte) 200)
			throw new UnsupportedTypeException("not supported type: " + type);

		int version = bb.get();
		int colCount = bb.getShort() & 0xffff;

		FileManifest manifest = new FileManifest();
		manifest.setVersion(version);

		for (int i = 0; i < colCount; i++) {
			int colId = bb.getShort() & 0xffff;
			int nameLength = bb.getShort() & 0xffff;
			byte[] nameBytes = new byte[nameLength];
			bb.get(nameBytes);

			int configCount = bb.getShort() & 0xffff;
			manifest.add(new CollectionEntry(colId, new String(nameBytes)));

			for (int j = 0; j < configCount; j++) {
				int docId = bb.getInt();
				long rev = bb.getLong();
				int index = bb.getInt();

				manifest.add(new ConfigEntry(colId, docId, rev, index));
			}
		}

		return manifest;
	}

	@Override
	public int lengthOf(Object value) {
		FileManifest manifest = (FileManifest) value;

		int total = 4; // type 1byte + version 1byte + col count 2byte
		try {
			for (String colName : manifest.getCollectionNames()) {
				// col id (2b), name len (2b), name, config count (2b)
				total += 6 + colName.getBytes("utf-8").length;
				total += 16 * manifest.getConfigEntries(colName).size();
			}
		} catch (UnsupportedEncodingException e) {
		}

		return total;
	}

	@Override
	public int getObjectLength(ByteBuffer bb) {
		return 0;
	}
}

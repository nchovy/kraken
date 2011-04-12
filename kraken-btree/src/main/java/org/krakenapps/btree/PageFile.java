/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.btree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.krakenapps.btree.types.CompositeKeyFactory;
import org.krakenapps.btree.types.IntegerKeyFactory;
import org.krakenapps.btree.types.LongKeyFactory;
import org.krakenapps.btree.types.StringKeyFactory;

public class PageFile {
	private static final String FORMAT_MARKER = "Kraken Page";
	/**
	 * format marker + page size + root page
	 */
	private static final int FILE_HEADER_SIZE = 64;
	private RandomAccessFile raf;
	private Schema schema;
	private int rootPage;

	public static PageFile create(File file, Schema schema) throws IOException {
		if (schema.getPageSize() < 0)
			throw new IllegalArgumentException("cannot use negative page size");

		RandomAccessFile raf = new RandomAccessFile(file, "rwd");
		try {
			// write file header
			byte[] b = FORMAT_MARKER.getBytes("utf-8");
			byte[] pad = new byte[16 - FORMAT_MARKER.length()];
			raf.write(b);
			raf.write(pad);
			raf.writeShort(schema.getPageSize());
			raf.writeInt(1); // root page

			int keyCount = schema.getKeyTypes().length;
			raf.write(keyCount);
			for (int i = 0; i < keyCount; i++)
				raf.write(encodeKeyType(schema.getKeyTypes()[i]));

			// create root page (force zero padding)
			raf.seek(FILE_HEADER_SIZE);
			raf.writeShort(PageType.LEAF);
			raf.seek(FILE_HEADER_SIZE + schema.getPageSize() - 1);
			raf.writeByte(0);
		} finally {
			raf.close();
		}

		return new PageFile(file);
	}

	private static int encodeKeyType(Class<?> clazz) {
		if (clazz == Integer.class)
			return 1;
		else if (clazz == Long.class)
			return 2;
		else if (clazz == String.class)
			return 3;
		return 0;
	}

	private static Class<?> decodeKeyType(int type) {
		switch (type) {
		case 1:
			return Integer.class;
		case 2:
			return Long.class;
		case 3:
			return String.class;
		}

		throw new IllegalArgumentException("illegal key type: " + type);
	}

	public PageFile(File file) throws IOException {
		if (!file.exists())
			throw new FileNotFoundException();

		this.raf = new RandomAccessFile(file, "rwd");
		readFileHeader();
	}

	public Schema getSchema() {
		return schema;
	}

	public int getRootPage() {
		return rootPage;
	}

	public void setRootPage(int pageNumber) throws IOException {
		raf.seek(18);
		raf.writeInt(pageNumber);

		rootPage = pageNumber;
	}

	public void setRowValueFactory(RowValueFactory valueFactory) {
		this.schema.setRowValueFactory(valueFactory);
	}

	public int getPageCount() throws IOException {
		long length = raf.length() - FILE_HEADER_SIZE;
		return (int) Math.ceil(length / (float) schema.getPageSize());
	}

	private void readFileHeader() throws IOException {
		// marker: 16
		// page size: 2
		// root page: 4
		// key count (1) + keys
		byte[] b = new byte[FORMAT_MARKER.length()];
		raf.read(b);
		String mark = new String(b, "utf-8");
		if (!mark.equals(FORMAT_MARKER))
			throw new IOException("invalid file format");

		raf.seek(16);
		int pageSize = raf.readShort() & 0xffff;
		this.rootPage = raf.readInt();
		int keyCount = raf.read();

		Class<?>[] keys = new Class<?>[keyCount];
		for (int i = 0; i < keyCount; i++) {
			keys[i] = decodeKeyType(raf.read());
		}

		this.schema = new Schema(pageSize, keys);
		setupRowKeyFactory(keys);
	}

	private void setupRowKeyFactory(Class<?>[] keys) {
		if (keys.length == 1) {
			Class<?> type = keys[0];

			if (type == Integer.class)
				schema.setRowKeyFactory(new IntegerKeyFactory());
			else if (type == Long.class)
				schema.setRowKeyFactory(new LongKeyFactory());
			else if (type == String.class)
				schema.setRowKeyFactory(new StringKeyFactory());
		} else {
			schema.setRowKeyFactory(new CompositeKeyFactory(keys));
		}
	}

	public void updateRootPage(int pageNumber) throws IOException {
		raf.seek(18);
		raf.writeInt(pageNumber);
	}

	public void write(Page page) throws IOException {
		if (page.getNumber() <= 0)
			throw new IllegalArgumentException("page number should be positive: " + page.getNumber());

		Schema schema = page.getSchema();
		byte[] data = page.getData();

		raf.seek(FILE_HEADER_SIZE + (page.getNumber() - 1) * schema.getPageSize());
		raf.write(data, 0, data.length);
	}

	public Page read(int pageNumber) throws IOException {
		if (pageNumber <= 0)
			throw new IllegalArgumentException("page number should be positive: " + pageNumber);

		int pageSize = schema.getPageSize();
		byte[] b = new byte[pageSize];

		raf.seek(FILE_HEADER_SIZE + (pageNumber - 1) * pageSize);
		raf.read(b);

		return new Page(pageNumber, schema, b);
	}

	public void close() throws IOException {
		raf.close();
	}
}

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
package org.krakenapps.confdb.file;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.ConfigEntry;

public class FileManifestCodecTest {
	@Test
	public void testCodec() {
		FileManifest manifest = getManifest();
		System.out.println(manifest);

		FileManifestCodec codec = new FileManifestCodec();
		int len = EncodingRule.lengthOf(manifest, codec);
		System.out.println("total len: " + len);

		ByteBuffer bb = ByteBuffer.allocate(len);

		EncodingRule.encode(bb, manifest, codec);
		bb.flip();

		Object decoded = EncodingRule.decode(bb, codec);
		System.out.println(decoded);
	}

	private FileManifest getManifest() {
		FileManifest manifest = new FileManifest();
		manifest.setVersion(2);
		manifest.setId(1);
		manifest.add(new CollectionEntry(1, "col1"));
		manifest.add(new ConfigEntry(1, 1, 1));
		manifest.add(new ConfigEntry(1, 2, 1));

		manifest.add(new CollectionEntry(2, "col2"));
		manifest.add(new ConfigEntry(2, 1, 1));
		manifest.add(new ConfigEntry(2, 2, 1));
		manifest.add(new ConfigEntry(2, 3, 2));

		return manifest;
	}
}

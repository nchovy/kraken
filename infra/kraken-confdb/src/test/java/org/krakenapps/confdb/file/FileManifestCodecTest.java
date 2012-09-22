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

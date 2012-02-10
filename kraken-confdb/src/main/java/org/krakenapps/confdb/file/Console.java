package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;

import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.Manifest;

public class Console {
	public static void main(String[] args) throws IOException {
		new Console().run(args);
	}

	public void run(String[] args) throws IOException {
		FileConfigDatabase db = new FileConfigDatabase(new File("."), args[0]);
		Integer rev = null;
		if (args.length > 1)
			rev = Integer.valueOf(args[1]);

		Manifest manifest = db.getManifest(rev);

		System.out.println("Collections");
		for (String name : manifest.getCollectionNames()) {
			CollectionEntry e = manifest.getCollectionEntry(name);
			System.out.println(e);
		}
	}
}

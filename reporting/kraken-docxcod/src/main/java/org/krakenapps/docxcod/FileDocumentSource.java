package org.krakenapps.docxcod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileDocumentSource {
	private File f = null;

	public FileDocumentSource(File inputFile) {
		this.f = inputFile;
	}

	public InputStream getInputStream() throws FileNotFoundException {
		return new FileInputStream(f);
	}
	
	public File getFile() { return f; }
}

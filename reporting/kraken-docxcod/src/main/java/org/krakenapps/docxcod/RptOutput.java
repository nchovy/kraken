package org.krakenapps.docxcod;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class RptOutput {
	private File outputFile;

	public void file(){}
	
	public FileDocumentSource createDocumentSource() {
		return null;
	}
	
	public void setFile(File file){
		outputFile = file;
	}

	public InputStream createInputStream() {
		return new ByteArrayInputStream(new byte[0]); 
	}
}

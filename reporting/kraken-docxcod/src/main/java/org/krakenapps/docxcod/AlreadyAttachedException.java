package org.krakenapps.docxcod;

public class AlreadyAttachedException extends RuntimeException {

	public AlreadyAttachedException(String absolutePath) {
		super(absolutePath);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}

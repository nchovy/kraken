package org.krakenapps.logstorage.engine;

import java.io.IOException;


public class InvalidLogFileHeaderException extends IOException {

	public InvalidLogFileHeaderException(Throwable e) {
		super(e);
	}

	public InvalidLogFileHeaderException(String string) {
		super(string);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}

package org.krakenapps.util.directoryfile.exceptions;

import java.io.IOException;

@SuppressWarnings("serial")
public class InvalidParameterexception extends IOException {

	public InvalidParameterexception(String subpath) {
		super("path input: " + subpath);
	}

}

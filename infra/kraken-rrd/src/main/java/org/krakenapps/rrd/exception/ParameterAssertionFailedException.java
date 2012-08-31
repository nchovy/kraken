package org.krakenapps.rrd.exception;

import java.io.IOException;

public class ParameterAssertionFailedException extends IOException {
	private static final long serialVersionUID = -3541379481680743239L;

	public ParameterAssertionFailedException(String string, Object pdpPerRow) {
		super(String.format("%s, %s", string, pdpPerRow.toString()));
	}
}

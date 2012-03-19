package org.krakenapps.rrd;

@SuppressWarnings("serial")
public class ParameterAssertionFailedException extends Exception {

	public ParameterAssertionFailedException(String string, Object pdpPerRow) {
		super(String.format("%s, %s", string, pdpPerRow.toString()));
	}
}

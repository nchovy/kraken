package org.krakenapps.datasource;

public interface DataConverter {
	String getName();
	
	String getInputType();

	String getOutputType();

	Object convert(Object o);
}

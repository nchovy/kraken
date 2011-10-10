package org.krakenapps.confdb;

public @interface ConfigElement {
	String key();

	boolean ignore() default false;
}

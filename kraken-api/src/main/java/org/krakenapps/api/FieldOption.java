package org.krakenapps.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldOption {
	boolean skip() default false;

	boolean nullable() default true;

	String name() default "";

	int length() default 0;
}

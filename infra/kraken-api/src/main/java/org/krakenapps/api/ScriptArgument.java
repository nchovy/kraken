package org.krakenapps.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScriptArgument {
	String name();

	String type() default "string";

	String description() default "";

	boolean optional() default false;

	Class<?> autocompletion() default ScriptAutoCompletionHelper.class;
}

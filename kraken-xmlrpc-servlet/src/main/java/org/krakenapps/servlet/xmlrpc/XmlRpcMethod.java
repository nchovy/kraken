package org.krakenapps.servlet.xmlrpc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface XmlRpcMethod {
	String alias() default "";
	String method() default "";
}

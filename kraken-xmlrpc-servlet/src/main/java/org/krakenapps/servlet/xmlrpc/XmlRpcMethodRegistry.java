package org.krakenapps.servlet.xmlrpc;

import java.util.Collection;

public interface XmlRpcMethodRegistry {
	Object dispatch(String method, Object[] parameters) throws Exception;

	Collection<String> getMethods();
}

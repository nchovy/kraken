package org.krakenapps.servlet.csv;

import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.http.NamespaceException;

public interface CsvHttpServiceApi {
	void registerServlet(String serverId, String pathSpec) throws ServletException, NamespaceException;

	Object invokeCsvMethod(String filterId, String methodName, HttpServletRequest req) throws Exception;

	Object invokeCsvMethod(String filterId, String methodName, Map<String, Object> params) throws Exception;

	Map<String, Set<String>> getAvailableFilters();
}

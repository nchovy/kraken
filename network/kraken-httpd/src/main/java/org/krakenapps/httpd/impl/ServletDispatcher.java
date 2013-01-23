/*
 * Copyright 2012 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.httpd.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletDispatcher {
	private final Logger logger = LoggerFactory.getLogger(ServletDispatcher.class.getName());

	/**
	 * servlet name to servlet mappings
	 */
	private ConcurrentMap<String, Servlet> servlets;
	/**
	 * 
	 * servlet name to registration mappings
	 */
	private ConcurrentMap<String, ServletRegistration> regs;

	/**
	 * fast exact path to servlet mappings
	 */
	private ConcurrentMap<String, ServletRegistration> exactMappings;

	/**
	 * servlet name to url patterns
	 */
	private ConcurrentMap<String, CopyOnWriteArraySet<String>> urlMappings;

	public ServletDispatcher() {
		servlets = new ConcurrentHashMap<String, Servlet>();
		regs = new ConcurrentHashMap<String, ServletRegistration>();
		exactMappings = new ConcurrentHashMap<String, ServletRegistration>();
		urlMappings = new ConcurrentHashMap<String, CopyOnWriteArraySet<String>>();
	}

	public Collection<String> getServletNames() {
		return servlets.keySet();
	}

	public Collection<Servlet> getServlets() {
		return servlets.values();
	}

	public Servlet getServlet(String name) {
		return servlets.get(name);
	}

	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return new HashMap<String, ServletRegistration>(regs);
	}

	public ServletRegistration getServletRegistration(String servletName) {
		return regs.get(servletName);
	}

	public ServletRegistration addServlet(String servletName, Servlet servlet) {
		Servlet old = servlets.putIfAbsent(servletName, servlet);
		if (old != null)
			throw new IllegalStateException("duplicated servlet name: " + servletName);

		ServletRegistrationImpl reg = new ServletRegistrationImpl(servletName, this);
		regs.putIfAbsent(servletName, reg);
		return reg;
	}

	public boolean removeServlet(String servletName) {
		Servlet servlet = servlets.remove(servletName);
		if (servlet != null) {
			// remove from registrations
			regs.remove(servletName);

			// remove from url pattern
			urlMappings.remove(servletName);

			// remove from exact mappings
			List<String> evicts = new ArrayList<String>();
			for (String path : exactMappings.keySet()) {
				ServletRegistration reg = exactMappings.get(path);
				if (reg.getName().equals(servletName))
					evicts.add(path);
			}

			for (String evict : evicts)
				exactMappings.remove(evict);
		}

		return servlet != null;
	}

	/**
	 * @param path
	 *            the url path excluding servlet context path and query string.
	 * @return the match result. null if no servlet found
	 */
	public ServletMatchResult matches(String path) {
		// exact match first
		ServletRegistration exactMatch = exactMappings.get(path);
		if (exactMatch != null) {
			Servlet servlet = servlets.get(exactMatch.getName());
			return new ServletMatchResult(servlet, exactMatch, path, "");
		}

		// try longest match and extension match
		String longest = null;
		ServletMatchResult r = null;
		for (ServletRegistration reg : regs.values()) {
			String servletPath = matchLongest(path, reg);
			if (servletPath != null) {
				if (longest == null || (longest.length() < servletPath.length())) {
					Servlet servlet = servlets.get(reg.getName());
					String pathInfo = null;
					if (!path.contains("?"))
						pathInfo = path.substring(servletPath.length());
					else
						pathInfo = path.substring(servletPath.length(), path.indexOf("?"));
					r = new ServletMatchResult(servlet, reg, servletPath, pathInfo);
					longest = servletPath;
				}
			}
		}

		return r;
	}

	/**
	 * @param reg
	 *            the servlet registration
	 * @return the longest match servlet path
	 */
	public String matchLongest(String path, ServletRegistration reg) {
		String longest = null;
		for (String mapping : reg.getMappings()) {
			String p = mapping;
			String ext = null;
			int pos = mapping.lastIndexOf("*");

			if (pos >= 0) {
				p = mapping.substring(0, pos);

				if (mapping.length() > (pos + 1) && mapping.charAt(pos + 1) == '.')
					ext = mapping.substring(pos + 1);
			}

			logger.debug("kraken httpd: check name [{}], mapping [{}] ext [{}] for input path [{}], match path [{}]",
					new Object[] { reg.getName(), mapping, ext, path, p });

			if (matchPath(path, p)) {
				if (ext != null && !path.endsWith(ext))
					continue;

				if (longest == null)
					longest = p;
				else if (longest.length() < p.length())
					longest = p;
			}
		}

		logger.trace("kraken httpd: returning longest mapping [{}]", longest);

		if (longest != null && longest.endsWith("/"))
			return longest.substring(0, longest.length() - 1);

		return longest;
	}

	private boolean matchPath(String path, String mapping) {
		if (mapping.isEmpty())
			return true;

		if (mapping.endsWith("/")) {
			if (path.startsWith(mapping))
				return true;
			else if (path.equals(mapping.substring(0, mapping.length() - 1)))
				return true;
			return false;
		}

		return path.startsWith(mapping);
	}

	public Collection<String> getMappings(String name) {
		return urlMappings.get(name);
	}

	public Set<String> addMapping(String name, String[] urlPatterns) {
		Set<String> failPatterns = new HashSet<String>();
		Set<String> normalizedUrlPatterns = new HashSet<String>();

		for (String p : urlPatterns) {
			if (countAsterisk(p) > 1)
				failPatterns.add(p);
			else
				normalizedUrlPatterns.add(normalizePattern(p));
		}

		ServletRegistration reg = regs.get(name);
		// cannot add mappings, return failed patterns
		if (reg == null)
			return new HashSet<String>(normalizedUrlPatterns);

		// check other url patterns
		for (String urlPattern : normalizedUrlPatterns) {
			for (CopyOnWriteArraySet<String> mappings : urlMappings.values()) {
				if (mappings.contains(urlPattern))
					failPatterns.add(urlPattern);
			}
		}

		CopyOnWriteArraySet<String> target = new CopyOnWriteArraySet<String>();
		CopyOnWriteArraySet<String> old = urlMappings.putIfAbsent(name, target);
		if (old != null)
			target = old;

		for (String urlPattern : normalizedUrlPatterns) {
			if (failPatterns.contains(urlPattern))
				continue;

			if (!urlPattern.contains("*"))
				exactMappings.put(urlPattern, reg);
			else
				target.add(urlPattern);
		}

		return failPatterns;
	}

	private int countAsterisk(String pattern) {
		int count = 0;
		for (int i = 0; i < pattern.length(); i++)
			if (pattern.charAt(i) == '*')
				count++;
		return count;
	}

	private String normalizePattern(String mapping) {
		int pos = mapping.lastIndexOf('/');

		String front = mapping.substring(0, pos + 1);
		String rear = mapping.substring(pos + 1);

		try {
			return new URI("file://", front, null).normalize().getPath() + rear;
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("  Exact Mappings\n");
		for (String path : exactMappings.keySet()) {
			sb.append("    ");
			sb.append(path);
			sb.append(" => Servlet ");
			sb.append(exactMappings.get(path).getName());
			sb.append("\n");
		}

		for (ServletRegistration reg : regs.values()) {
			sb.append("  Servlet ");
			sb.append(reg.getName());
			sb.append("\n");
			for (String pattern : urlMappings.get(reg.getName())) {
				sb.append("    ");
				sb.append(pattern);
				sb.append("\n");
			}
		}

		return sb.toString();
	}
}

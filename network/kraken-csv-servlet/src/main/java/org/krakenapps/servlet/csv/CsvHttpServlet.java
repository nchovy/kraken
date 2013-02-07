/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.servlet.csv;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvHttpServlet extends HttpServlet {
	final Logger logger = LoggerFactory.getLogger(CsvHttpServlet.class.getName());
	private static final long serialVersionUID = 1L;
	private CsvHttpServiceApi api;

	public CsvHttpServlet(CsvHttpServiceApi api) {
		this.api = api;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.debug("csv servlet pathinfo: " + req.getPathInfo());
		String[] tokens = req.getPathInfo().split("/");
		if (tokens.length != 3)
			return;

		String filterId = tokens[1];
		String methodName = tokens[2];

		List<CsvRow> csvRows = getCsvRows(filterId, methodName, req);
		if (csvRows == null) {
			resp.sendError(404);
			return;
		}

		resp.setCharacterEncoding("euc-kr");
		resp.setHeader("Content-Disposition", "inline; filename=\"" + methodName + ".csv\"");
		resp.setHeader("Cache-Control", "no-cache, must-revalidate");
		resp.setHeader("Pragma", "no-cache");
		resp.setContentType("text/csv;charset=euc-kr");

		printCsv(resp.getWriter(), csvRows);
		resp.getWriter().close();
	}

	@SuppressWarnings("unchecked")
	private List<CsvRow> getCsvRows(String filterId, String methodName, HttpServletRequest req) {
		try {
			List<CsvRow> csvRows = (List<CsvRow>) api.invokeCsvMethod(filterId, methodName, req);
			if (csvRows == null) {
				logger.warn("csv servlet null returned.");
			}
			return csvRows;
		} catch (NoSuchMethodException e) {
			try {
				Map<String, Object> params = buildParameterMap(req);
				List<CsvRow> csvRows = (List<CsvRow>) api.invokeCsvMethod(filterId, methodName, params);
				if (csvRows == null) {
					logger.warn("csv servlet null returned.");
				}
				return csvRows;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			logger.error("csv get error", e);
		}
		return null;
	}

	private void printCsv(PrintWriter out, List<CsvRow> csvRows) {
		for (CsvRow row : csvRows) {
			for (int i = 0; i < row.size(); i++) {
				if (i == 0) {
					out.print(escape(row.get(i)));
				} else {
					out.print(",");
					out.print(escape(row.get(i)));
				}
			}

			out.println();
		}
	}

	private String escape(String v) {
		if (v == null)
			return "";

		if (v.indexOf(",") >= 0)
			return "\"" + v + "\"";
		return v;
	}

	private Map<String, Object> buildParameterMap(HttpServletRequest req) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		@SuppressWarnings("rawtypes")
		Enumeration it = req.getParameterNames();
		while (it.hasMoreElements()) {
			String name = (String) it.nextElement();
			Object value = req.getParameter(name);
			paramMap.put(name, value);
		}
		return paramMap;
	}
}

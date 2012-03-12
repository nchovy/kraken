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
package org.krakenapps.servlet.json;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonHttpServlet extends HttpServlet {
	final Logger logger = LoggerFactory.getLogger(JsonHttpServlet.class.getName());
	private static final long serialVersionUID = 1L;

	private JsonHttpServiceApi manager;

	public JsonHttpServlet(JsonHttpServiceApi manager) {
		this.manager = manager;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json;charset=UTF-8");

		try {
			logger.debug("json servlet pathinfo: " + req.getPathInfo());
			String[] tokens = req.getPathInfo().split("/");
			if (tokens.length != 3) {
				System.out.println("invalid token number");
				return;
			}

			String filterId = tokens[1];
			String methodName = tokens[2];
			Map<String, Object> params = buildParameterMap(req);
			Object result = manager.invokeJsonMethod(filterId, methodName, params);

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("result", result);
			resp.getWriter().print(jsonObject.toString());
			resp.getWriter().close();

		} catch (Exception e) {
			logger.warn(e.toString());
		}

	}

	@SuppressWarnings("rawtypes")
	private Map<String, Object> buildParameterMap(HttpServletRequest req) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Enumeration it = req.getParameterNames();
		while (it.hasMoreElements()) {
			String name = (String) it.nextElement();
			Object value = req.getParameter(name);
			paramMap.put(name, value);
		}
		return paramMap;
	}
}

/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.webfx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Router {
	private List<Route> routes = new ArrayList<Route>();

	public Router() {
	}

	public ControllerAction map(String pathInfo) {
		Map<String, String> params = new HashMap<String, String>();
		for (Route r : routes)
			if (isMapped(r, pathInfo, params))
				return new ControllerAction(r.target.get("controller"), r.target.get("action"));

		return null;
	}

	private boolean isMapped(Route r, String path, Map<String, String> params) {
		params.clear();
		int i = 0;
		Scanner s = new Scanner(path).useDelimiter("/");
		while (s.hasNext()) {
			String token = s.next();
			if (i >= r.pathTokens.size())
				return false;

			if (r.pathTokens.get(i).startsWith(":")) {
				// TODO: validate using regexp
				params.put(r.pathTokens.get(i).substring(1), token);
			} else if (!r.pathTokens.get(i).equals(token)) {
				return false;
			}
			i++;
		}

		return true;
	}

	public void add(Resource r) {
		String name = r.getName();
		String path = "/" + name;
		if (path.endsWith("Controller"))
			path = path.substring(0, path.length() - "Controller".length());

		path = path.toLowerCase();

		routes.add(new Route("GET", r.getPathPrefix() + path, name, "index"));
		routes.add(new Route("GET", r.getPathPrefix() + path + "/:id", name, "show"));
	}

	public void connect(String path, ControllerAction action) {
		routes.add(new Route("GET", path, action.getController(), action.getAction()));
	}

	private static class Route {
		private String method;
		private List<String> pathTokens;
		private Map<String, String> target = new HashMap<String, String>();

		// TODO: regex validators

		public Route(String method, String path, String controller, String action) {
			this.method = method;
			setPathTokens(path);

			target.put("controller", controller);
			target.put("action", action);
		}

		private void setPathTokens(String path) {
			pathTokens = new ArrayList<String>();
			Scanner scanner = new Scanner(path).useDelimiter("/");
			while (scanner.hasNext()) {
				String next = scanner.next();
				if (!next.isEmpty())
					pathTokens.add(next);
			}
		}
	}
}

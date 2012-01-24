package org.krakenapps.webfx;

import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class.getName());

	private File baseDir;
	private BundleContext bc;
	private GroovyScriptEngine engine;
	private GroovyShell shell;
	private Map<String, Object> data;
	private Router router;

	public DispatcherServlet(BundleContext bc, File baseDir) {
		this.bc = bc;
		this.baseDir = baseDir;
		this.router = new Router();
	}

	public void start() throws IOException {

		File controllerDir = new File(baseDir, "app/controllers");
		engine = new GroovyScriptEngine(new String[] { controllerDir.getAbsolutePath() });
		engine.getGroovyClassLoader().setShouldRecompile(true);

		shell = new GroovyShell(engine.getGroovyClassLoader());
		data = new HashMap<String, Object>();
		data.put("linkTo", shell.evaluate("def c = { s -> controller.linkTo(s) } "));
		data.put("include", shell.evaluate("def c = { s -> controller.include(s) } "));
		data.put("osgi", shell.evaluate("def c = { s -> controller.osgi(s) }"));
		data.put("yield", shell.evaluate("def c = { s -> controller.yield(s) }"));
		data.put("bc", bc);
	}

	public void stop() {
		engine.getGroovyClassLoader().clearCache();
	}

	public Router getRouter() {
		return router;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			logger.trace("kraken webfx: GET request [{}]", req.getPathInfo());
			Date begin = new Date();
			invokeGroovy(req, resp, begin);
			long elapsed = new Date().getTime() - begin.getTime();
			logger.trace("kraken webfx: request [{}], {}ms used", req.getRequestURI(), elapsed);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void invokeGroovy(HttpServletRequest req, HttpServletResponse resp, Date begin) throws Exception {
		ControllerAction action = router.map(req.getPathInfo());
		if (action == null) {
			logger.trace("kraken webfx: mapping not found [{}]", req.getPathInfo());
			return;
		}

		Map<String, Object> d = new HashMap<String, Object>(data);
		d.put("controller", action.getController());
		d.put("action", action.getAction());

		String name = action.getController() + "Controller";
		Class<?> clazz = engine.getGroovyClassLoader().loadClass(name, true, false, true);
		Controller controller = (Controller) clazz.newInstance();
		controller.init(bc, shell, baseDir, d, req, resp);

		Method m = controller.getClass().getMethod(action.getAction());
		m.invoke(controller);
	}
}

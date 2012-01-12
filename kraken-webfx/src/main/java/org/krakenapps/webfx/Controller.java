package org.krakenapps.webfx;

import groovy.lang.GroovyShell;
import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Controller {
	private final Logger logger = LoggerFactory.getLogger(Controller.class.getName());
	private File baseDir;
	private GroovyShell sh;
	protected HttpServletRequest req;
	protected HttpServletResponse resp;
	protected Map<String, Object> data;
	protected BundleContext bc;

	public void init(BundleContext bc, GroovyShell sh, Map<String, Object> data, HttpServletRequest req,
			HttpServletResponse resp) {
		this.bc = bc;
		this.req = req;
		this.resp = resp;
		this.data = data;
		this.sh = sh;
		sh.setProperty("controller", this);

		String krakenDir = System.getProperty("kraken.data.dir");
		baseDir = new File(krakenDir, "kraken-webfx/sample");
	}

	protected void render() {
		String s = renderLayout();
		try {
			resp.getWriter().print(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void render(String viewName, String action) {
		// check partial, file, controller and action, template, and so on
		String s = renderView(viewName, action);
		try {
			resp.getWriter().print(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String renderLayout() {
		try {
			SimpleTemplateEngine te = new SimpleTemplateEngine(sh);
			File layoutDir = new File(baseDir, "app/views/layouts");
			layoutDir.mkdirs();
			Template t = te.createTemplate(new File(layoutDir, "application.html"));
			Writable rendered = t.make(data);
			return rendered.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private String renderView(String controller, String action) {
		try {
			File viewDir = new File(baseDir, "app/views");
			viewDir.mkdirs();
			SimpleTemplateEngine te = new SimpleTemplateEngine(sh);
			Template t = te.createTemplate(new File(viewDir, controller + "/" + action + ".html"));
			Writable rendered = t.make(data);
			return rendered.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private String renderFile(String path) {
		try {
			File viewDir = new File(baseDir, "app/views");
			viewDir.mkdirs();
			File f = new File(viewDir, path);

			SimpleTemplateEngine te = new SimpleTemplateEngine(sh);
			Template t = te.createTemplate(f);
			Writable rendered = t.make(data);
			return rendered.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	protected void redirectTo(String url) {
		try {
			resp.sendRedirect(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected Object linkTo(Object... params) {
		return "http://" + params[0];
	}

	protected String include(Object... params) {
		return renderFile((String) params[0]);
	}

	protected String yield(Object... params) {
		String controller = (String) data.get("controller");
		String action = (String) data.get("action");
		logger.info("kraken webfx: yielding [{}]", controller);
		return renderView(controller.toLowerCase(), action);
	}

	protected Object osgi(Object... params) {
		ServiceReference ref = bc.getServiceReference((String) params[0]);
		if (ref == null)
			return null;

		return bc.getService(ref);
	}

	@Override
	public String toString() {
		return "Hooray!!";
	}

}

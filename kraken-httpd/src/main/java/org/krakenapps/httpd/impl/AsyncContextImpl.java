package org.krakenapps.httpd.impl;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AsyncContextImpl implements AsyncContext {
	private HttpServletRequest req;
	private HttpServletResponse resp;
	private long timeout;

	public AsyncContextImpl(HttpServletRequest req, HttpServletResponse resp) {
		this.req = req;
		this.resp = resp;
	}

	@Override
	public void start(Runnable run) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasOriginalRequestAndResponse() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ServletRequest getRequest() {
		return req;
	}

	@Override
	public ServletResponse getResponse() {
		return resp;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public void complete() {
		try {
			resp.getOutputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dispatch() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispatch(String path) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispatch(ServletContext context, String path) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addListener(AsyncListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
		// TODO Auto-generated method stub

	}

}

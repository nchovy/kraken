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
package org.slf4j.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

public class KrakenLogger extends org.apache.felix.framework.Logger implements org.slf4j.Logger, Serializable {
	private static final long serialVersionUID = 1L;
	private static long bootTime = System.currentTimeMillis();
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static int INFO = Priority.INFO_INT;
	private static int WARN = Priority.WARN_INT;
	private static int ERROR = Priority.ERROR_INT;
	private static int DEBUG = Priority.DEBUG_INT;
	private static int TRACE = Priority.DEBUG_INT;

	private String name;
	private boolean isDebugEnabled = false;
	private boolean isTraceEnabled = false;
	private boolean isInfoEnabled = true;
	private boolean isWarnEnabled = true;
	private boolean isErrorEnabled = true;

	private BlockingQueue<KrakenLog> queue;

	public KrakenLogger(String name, BlockingQueue<KrakenLog> queue) {
		this.name = name;
		this.queue = queue;
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		if (isDebugEnabled)
			formatAndLog(DEBUG, format, arg1, arg2);
	}

	@Override
	public void debug(String format, Object arg) {
		if (isDebugEnabled)
			formatAndLog(DEBUG, format, arg, null);
	}

	@Override
	public void debug(String format, Object[] argArray) {
		if (isDebugEnabled)
			formatAndLog(DEBUG, format, argArray);
	}

	@Override
	public void debug(String msg, Throwable t) {
		if (isDebugEnabled)
			internalLog(DEBUG, msg, t);
	}

	@Override
	public void debug(String msg) {
		if (isDebugEnabled)
			internalLog(DEBUG, msg, null);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		if (isErrorEnabled)
			formatAndLog(ERROR, format, arg1, arg2);
	}

	@Override
	public void error(String format, Object arg) {
		if (isErrorEnabled)
			formatAndLog(ERROR, format, arg, null);
	}

	@Override
	public void error(String format, Object[] argArray) {
		if (isErrorEnabled)
			formatAndLog(ERROR, format, argArray);
	}

	@Override
	public void error(String msg, Throwable t) {
		if (isErrorEnabled)
			internalLog(ERROR, msg, t);
	}

	@Override
	public void error(String msg) {
		if (isErrorEnabled)
			internalLog(ERROR, msg, null);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		if (isInfoEnabled)
			formatAndLog(INFO, format, arg1, arg2);
	}

	@Override
	public void info(String format, Object arg) {
		if (isInfoEnabled)
			formatAndLog(INFO, format, arg, null);
	}

	@Override
	public void info(String format, Object[] argArray) {
		if (isInfoEnabled)
			formatAndLog(INFO, format, argArray);
	}

	@Override
	public void info(String msg, Throwable t) {
		if (isInfoEnabled)
			internalLog(INFO, msg, t);
	}

	@Override
	public void info(String msg) {
		if (isInfoEnabled)
			internalLog(INFO, msg, null);
	}

	@Override
	public boolean isDebugEnabled() {
		return isDebugEnabled;
	}

	@Override
	public boolean isErrorEnabled() {
		return isErrorEnabled;
	}

	@Override
	public boolean isInfoEnabled() {
		return isInfoEnabled;
	}

	@Override
	public boolean isTraceEnabled() {
		return isTraceEnabled;
	}

	@Override
	public boolean isWarnEnabled() {
		return isWarnEnabled;
	}

	public void setDebugEnabled(boolean isDebugEnabled) {
		this.isDebugEnabled = isDebugEnabled;
	}

	public void setTraceEnabled(boolean isTraceEnabled) {
		this.isTraceEnabled = isTraceEnabled;
	}

	public void setInfoEnabled(boolean isInfoEnabled) {
		this.isInfoEnabled = isInfoEnabled;
	}

	public void setWarnEnabled(boolean isWarnEnabled) {
		this.isWarnEnabled = isWarnEnabled;
	}

	public void setErrorEnabled(boolean isErrorEnabled) {
		this.isErrorEnabled = isErrorEnabled;
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		if (isTraceEnabled)
			formatAndLog(TRACE, format, arg1, arg2);
	}

	@Override
	public void trace(String format, Object arg) {
		if (isTraceEnabled)
			formatAndLog(TRACE, format, arg, null);
	}

	@Override
	public void trace(String format, Object[] argArray) {
		if (isTraceEnabled)
			formatAndLog(TRACE, format, argArray);
	}

	@Override
	public void trace(String msg, Throwable t) {
		if (isTraceEnabled)
			internalLog(TRACE, msg, t);
	}

	@Override
	public void trace(String msg) {
		if (isTraceEnabled)
			internalLog(TRACE, msg, null);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		if (isWarnEnabled)
			formatAndLog(WARN, format, arg1, arg2);
	}

	@Override
	public void warn(String format, Object arg) {
		if (isWarnEnabled)
			formatAndLog(WARN, format, arg, null);
	}

	@Override
	public void warn(String format, Object[] argArray) {
		if (isWarnEnabled)
			formatAndLog(WARN, format, argArray);
	}

	@Override
	public void warn(String msg, Throwable t) {
		if (isWarnEnabled)
			internalLog(WARN, msg, t);
	}

	@Override
	public void warn(String msg) {
		if (isWarnEnabled)
			internalLog(WARN, msg, null);
	}

	@SuppressWarnings("deprecation")
	private void internalLog(int level, String message, Throwable t) {
		Date date = new Date();
		Priority priority = Priority.toPriority(level);
		if (t != null) {
			getLogger().log(priority, message + "\n" + makeStackTrace(t));
		} else {
			getLogger().log(priority, message);
		}

		long timeOffset = System.currentTimeMillis() - bootTime;

		queue.add(new KrakenLog(name, timeOffset, date, level, message, t));
	}

	private String makeStackTrace(Throwable t) {
		if (t == null)
			return "";
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(out));
			out.flush();
			return new String(out.toByteArray());
		} catch (Exception e) {
			return "";
		}
	}

	private Logger getLogger() {
		return Logger.getLogger(name);
	}

	/**
	 * For formatted messages, first substitute arguments and then log.
	 * 
	 * @param level
	 * @param format
	 * @param param1
	 * @param param2
	 */
	private void formatAndLog(int level, String format, Object arg1, Object arg2) {
		String message = MessageFormatter.format(format, arg1, arg2);
		internalLog(level, message, null);
	}

	/**
	 * For formatted messages, first substitute arguments and then log.
	 * 
	 * @param level
	 * @param format
	 * @param argArray
	 */
	private void formatAndLog(int level, String format, Object[] argArray) {
		String message = MessageFormatter.arrayFormat(format, argArray);
		internalLog(level, message, null);
	}

	/**
	 * Marker ignoring base.
	 */

	@Override
	public void debug(Marker marker, String msg) {
		debug(msg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		debug(format, arg);
	}

	@Override
	public void debug(Marker marker, String format, Object[] argArray) {
		debug(format, argArray);
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		debug(msg, t);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		debug(format, arg1, arg2);
	}

	@Override
	public void error(Marker marker, String msg) {
		error(msg);
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		error(format, arg);
	}

	@Override
	public void error(Marker marker, String format, Object[] argArray) {
		error(format, argArray);
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		error(msg, t);
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		error(format, arg1, arg2);
	}

	@Override
	public void info(Marker marker, String msg) {
		info(msg);
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		info(format, arg);
	}

	@Override
	public void info(Marker marker, String format, Object[] argArray) {
		info(format, argArray);
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		info(msg, t);
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		info(format, arg1, arg2);
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return isDebugEnabled;
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return isErrorEnabled;
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return isInfoEnabled;
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return isTraceEnabled;
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return isWarnEnabled;
	}

	@Override
	public void trace(Marker marker, String msg) {
		trace(msg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		trace(format, arg);
	}

	@Override
	public void trace(Marker marker, String format, Object[] argArray) {
		trace(format, argArray);
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		trace(msg, t);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		trace(format, arg1, arg2);
	}

	@Override
	public void warn(Marker marker, String msg) {
		warn(msg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		warn(format, arg);
	}

	@Override
	public void warn(Marker marker, String format, Object[] argArray) {
		warn(format, argArray);
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		warn(msg, t);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		warn(format, arg1, arg2);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * for felix logger.
	 */
	@Override
	protected void doLog(Bundle bundle, ServiceReference sr, int level, String msg, Throwable throwable) {
		internalLog(level, msg, throwable);
	}
}

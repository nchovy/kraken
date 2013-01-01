/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logdb.log4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.json.JSONException;
import org.json.JSONWriter;

public class HttpAppender extends AppenderSkeleton {
	private volatile boolean doStop;
	private Thread t;
	private ArrayBlockingQueue<String> logs;
	private String url;

	public HttpAppender() {
		logs = new ArrayBlockingQueue<String>(10000);
		t = new Thread(new HttpSender(), "Kraken LogDB Logger");
		t.start();
	}

	public void setURL(String url) {
		this.url = url;
	}

	@Override
	protected void append(LoggingEvent event) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
			StringWriter sw = new StringWriter();
			JSONWriter w = new JSONWriter(sw).object();

			LocationInfo l = event.getLocationInformation();
			w.key("_time").value(dateFormat.format(event.timeStamp));
			w.key("level").value(s(event.getLevel()));
			w.key("msg").value(s(event.getMessage()));
			w.key("ndc").value(event.getNDC());
			w.key("thread").value(event.getThreadName());
			w.key("class").value(l.getClassName());
			w.key("file").value(l.getFileName());
			w.key("line").value(l.getLineNumber());
			w.key("method").value(l.getMethodName());
			w.key("stacktrace").value(printStackTrace(event));
			w.endObject();

			logs.put(sw.toString());
		} catch (JSONException e) {
		} catch (InterruptedException e) {
		}
	}

	private void sendLogs() {
		if (url == null)
			return;

		if (logs.isEmpty())
			return;

		HttpURLConnection con = null;
		OutputStream os = null;
		try {
			URL u = new URL(url);
			con = (HttpURLConnection) u.openConnection();
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "text/json");
			os = con.getOutputStream();

			os.write("[".getBytes());
			int i = 0;
			while (true) {
				String s = logs.poll();
				if (s == null)
					break;

				if (i++ != 0)
					os.write(",".getBytes());

				os.write(s.getBytes("utf-8"));
			}
			os.write("]".getBytes());
			con.getResponseCode();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private String s(Object o) {
		if (o != null)
			return o.toString();
		return null;
	}

	@Override
	public void close() {
		doStop = true;
		t.interrupt();
		try {
			t.join(5000);
		} catch (InterruptedException e) {
		}
	}

	private String printStackTrace(LoggingEvent event) {
		if (event.getThrowableInformation() == null)
			return null;

		Throwable t = event.getThrowableInformation().getThrowable();
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		return sw.toString();
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	private class HttpSender implements Runnable {

		@Override
		public void run() {
			while (!doStop) {
				try {
					Thread.sleep(1000);
					sendLogs();
				} catch (InterruptedException e) {
				} catch (Throwable t) {
				}
			}
		}

	}
}

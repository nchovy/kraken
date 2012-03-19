/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.linux.api;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WtmpEntry {
	enum Type {
		Unknown, RunLevel, BootTime, NewTime, OldTime, InitProcess, LoginProcess, UserProcess, DeadProcess, Accounting;
	}

	private Type type;
	private int pid;
	private String user;
	private String host;
	private int session;
	private Date date;

	public WtmpEntry(Type type, Date date, int pid, String user, String host, int session) {
		this.type = type;
		this.date = date;
		this.pid = pid;
		this.user = user;
		this.host = host;
		this.session = session;
	}

	public Type getType() {
		return type;
	}

	public int getPid() {
		return pid;
	}

	public String getUser() {
		return user;
	}

	public String getHost() {
		return host;
	}

	public int getSession() {
		return session;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return String.format("type=%s, date=%s, pid=%d, user=%s, host=%s", type, dateFormat.format(date), pid, user,
				host);
	}

}

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
package org.krakenapps.ntp.msgbus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.krakenapps.ntp.NtpSyncService;

/**
 * @author delmitz
 */
@Component(name = "ntp-plugin")
@MsgbusPlugin
public class NtpPlugin {
	@Requires
	private NtpSyncService syncService;

	@MsgbusMethod
	public void nowDate(Request req, Response resp) {
		resp.put("date", new Date());
	}

	@MsgbusMethod
	public void getNtpClientConfig(Request req, Response resp) {
		resp.put("time_server", syncService.getTimeServer().getHostName());
		resp.put("timeout", syncService.getTimeout());
	}

	@MsgbusMethod
	public void setNtpClientConfig(Request req, Response resp) {
		String server = req.getString("time_server");
		int timeout = req.getInteger("timeout");
		try {
			syncService.setTimeServer(InetAddress.getByName(server));
			syncService.setTimeout(timeout);
		} catch (UnknownHostException e) {
			throw new MsgbusException("ntp", "unknown host");
		}
	}

	@MsgbusMethod
	public void sync(Request req, Response resp) {
		try {
			resp.put("synced_time", syncService.getNtpClient().sync());
		} catch (IOException e) {
			throw new MsgbusException("ntp", "ntp-sync-failed");
		}
	}

	@MsgbusMethod
	public void setSystemDate(Request req, Response resp) {
		int year = req.getInteger("year");
		int month = req.getInteger("month");
		int day = req.getInteger("day");
		int hour = req.getInteger("hour");
		int minute = req.getInteger("minute");
		int second = req.getInteger("second");
		String s = String.format("%d-%d-%d %d:%d:%d", year, month, day, hour, minute, second);
		try {
			Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s);
			syncService.getNtpClient().setSystemTime(d);
		} catch (ParseException e) {
			throw new MsgbusException("ntp", "date parse exception");
		} catch (IOException e) {
			throw new MsgbusException("ntp", "failed to setting system time");
		}
	}

}

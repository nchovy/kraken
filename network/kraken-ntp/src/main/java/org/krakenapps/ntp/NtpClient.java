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
package org.krakenapps.ntp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.krakenapps.ntp.impl.ServerTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author delmitz
 */
public class NtpClient {
	private static final String DEFAULT_TIME_SERVER = "pool.ntp.org";
	private static final int NTP_V3_PACKET = 3;
	private static final int CLIENT_MODE = 3;
	private static final int STRATUM_INDEX = 1;
	private static final int POLL_INDEX = 2;
	private static final int PRECISION_INDEX = 3;
	private static final int ROOT_DELAY_INDEX = 4;
	private static final int ROOT_DISPERSION_INDEX = 8;
	private static final int REFERENCE_IDENTIFIER_INDEX = 12;
	private static final int REFERENCE_TIMESTAMP_INDEX = 16;
	private static final int ORIGINATE_TIMESTAMP_INDEX = 24;
	private static final int RECEIVE_TIMESTAMP_INDEX = 32;
	private static final int TRANSMIT_TIMESTAMP_INDEX = 40;

	private Logger logger = LoggerFactory.getLogger(NtpClient.class);

	private InetAddress timeServer;
	private int timeout;

	public NtpClient() throws UnknownHostException {
		this(null);
	}

	public NtpClient(String timeServer) throws UnknownHostException {
		this(timeServer, 5000);
	}

	public NtpClient(String timeServer, int timeout) throws UnknownHostException {
		if (timeServer == null)
			timeServer = DEFAULT_TIME_SERVER;
		this.timeServer = InetAddress.getByName(timeServer);
		this.timeout = timeout;
	}

	public InetAddress getTimeServer() {
		return timeServer;
	}

	public void setTimeServer(InetAddress timeServer) {
		this.timeServer = timeServer;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setSystemTime(Date time) throws IOException {
		Date currentTime = new Date();

		String os = System.getProperty("os.name");
		if (os.contains("Windows")) {
			String newDate = new SimpleDateFormat("MM-dd-yy").format(time);
			Runtime.getRuntime().exec("cmd /c date " + newDate);

			String newTime = new SimpleDateFormat("HH:mm:ss.SSS").format(time);
			newTime = newTime.substring(0, newTime.length() - 1);
			Runtime.getRuntime().exec("cmd /c time " + newTime);
		} else if (os.contains("Linux")) {
			String newTime = new SimpleDateFormat("MMddHHmmyyyy.ss").format(time);
			Runtime.getRuntime().exec("date " + newTime);
		} else {
			// just pray
			String newTime = new SimpleDateFormat("MMddHHmmyyyy.ss").format(time);
			Runtime.getRuntime().exec("date " + newTime);
		}

		String newTime = new SimpleDateFormat("MMddHHmmyyyy.ss").format(time);
		logger.debug("kraken ntp: OS [{}] set system time {}", os, newTime);

		long diff = Math.abs((currentTime.getTime() - time.getTime()) / 60 * 1000);
		if (diff >= 30) {
			logger.info("kraken ntp: OS [{}] set recieve time [{}], current time [{}]", new Object[] { os, newTime,
					new SimpleDateFormat("MMddHHmmyyyy.ss").format(currentTime) });
		}

	}

	public Date sync() throws IOException {
		ServerTime time = getTime();
		setSystemTime(addOffset(time));
		logger.trace("kraken ntp: The time has been successfully synchronized with {} on {}", timeServer, new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss.SSS 'UTC'").format(time.getTransmit()));
		return time.getTransmit();
	}

	private ServerTime getTime() throws IOException {
		DatagramSocket socket = new DatagramSocket();
		try {
			socket.setSoTimeout(timeout);
			transmit(socket, timeServer, 123);
			ServerTime time = receive(socket);
			time.setDestination(getUtcTimeMillis());
			return time;
		} finally {
			socket.close();
		}
	}

	private void transmit(DatagramSocket socket, InetAddress addr, int port) throws IOException {
		byte[] buf = new byte[48];
		buf[0] |= NTP_V3_PACKET; // set NTP Packet version 3
		buf[0] |= (CLIENT_MODE << 3); // set Client mode

		long now = getUtcTimeMillis();
		now += 2209021200000L; // baseline 1970 to 1900

		long l = (now / 1000) << 32;
		l |= ((now % 1000) << 32) / 1000;
		for (int i = 7; i >= 0; i--) { // set Transmit Timestamp
			buf[TRANSMIT_TIMESTAMP_INDEX + i] = (byte) (l & 0xff);
			l >>>= 8;
		}

		DatagramPacket p = new DatagramPacket(buf, buf.length);
		p.setAddress(addr);
		p.setPort(port);
		socket.send(p);
	}

	private long getUtcTimeMillis() {
		return System.currentTimeMillis() - Calendar.getInstance().getTimeZone().getOffset(0);
	}

	private ServerTime receive(DatagramSocket socket) throws IOException, SocketTimeoutException {
		byte[] buf = new byte[48];
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		socket.receive(p);

		ServerTime time = new ServerTime();
		time.setStratum((int) buf[STRATUM_INDEX]);
		time.setPoll(buf[POLL_INDEX]);
		time.setPrecision(buf[PRECISION_INDEX]);
		time.setRootDelay(Arrays.copyOfRange(buf, ROOT_DELAY_INDEX, ROOT_DELAY_INDEX + 4));
		time.setRootDispersion(Arrays.copyOfRange(buf, ROOT_DISPERSION_INDEX, ROOT_DISPERSION_INDEX + 4));
		time.setReferenceIdentifier(Arrays.copyOfRange(buf, REFERENCE_IDENTIFIER_INDEX, REFERENCE_IDENTIFIER_INDEX + 4));
		time.setReference(ntpTimeToJavaDate(buf, REFERENCE_TIMESTAMP_INDEX));
		time.setOriginate(ntpTimeToJavaDate(buf, ORIGINATE_TIMESTAMP_INDEX));
		time.setReceive(ntpTimeToJavaDate(buf, RECEIVE_TIMESTAMP_INDEX));
		time.setTransmit(ntpTimeToJavaDate(buf, TRANSMIT_TIMESTAMP_INDEX));

		return time;
	}

	private long ntpTimeToJavaDate(byte[] buf, int offset) {
		long seconds = 0;
		for (int i = 0; i < 4; i++) {
			seconds = seconds << 8;
			seconds |= buf[offset + i] & 0xff;
		}
		seconds *= 1000L;
		seconds -= 2209021200000L; // baseline 1900 to 1970

		long fraction = 0;
		for (int i = 4; i < 8; i++) {
			fraction = fraction << 8;
			fraction |= buf[offset + i] & 0xff;
		}
		fraction *= 1000;
		fraction >>>= 32;

		return (seconds + fraction);
	}

	private Date addOffset(ServerTime time) {
		long millis = System.currentTimeMillis() + time.getClockOffset();
		return new Date(millis);
	}
}

package org.krakenapps.ftp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpClient {
	private Logger logger = LoggerFactory.getLogger(FtpClient.class.getName());
	private final int BUF_SIZE = 512;
	private final String CHARSET = "UTF-8";

	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private Vector<FtpServerMessage> messageLog;

	public FtpClient(String host) throws UnknownHostException, IOException {
		this(host, 21);
	}

	public FtpClient(String host, int port) throws UnknownHostException, IOException {
		this.socket = new Socket(host, port);
		this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), Charset.forName(CHARSET)));
		this.writer = new PrintWriter(socket.getOutputStream());
		this.messageLog = new Vector<FtpServerMessage>();
		if (!getMessage().isCode(220))
			throw new IOException("ftp connect failed");
	}

	public boolean isConnected() {
		return socket.isConnected();
	}

	public FtpServerMessage getLastMessage() {
		if (messageLog.size() == 0)
			return null;
		return messageLog.lastElement();
	}

	public boolean login(String user, String password) throws IOException {
		if (!send("USER " + user).isCode(331))
			return false;
		if (!send("PASS " + password).isCode(230))
			return false;
		return setBinary();
	}

	public String system() throws IOException {
		return send("SYST").getMessages()[0];
	}

	public boolean setAscii() throws IOException {
		return send("TYPE A").isCode(200);
	}

	public boolean setBinary() throws IOException {
		return send("TYPE I").isCode(200);
	}

	public String printWorkingDirectory() throws IOException {
		FtpServerMessage msg = send("PWD");
		if (!msg.isCode(257))
			return null;

		return extract(msg.getMessages()[0], "\"");
	}

	public boolean changeWorkingDirectory(String dir) throws IOException {
		return send("CWD " + dir).isCode(250);
	}

	public boolean changeToParentDirectory() throws IOException {
		return send("CDUP").isCode(200);
	}

	public String[] list() throws IOException {
		Socket data = openDataPort();
		List<String> list = new ArrayList<String>();

		if (!send("LIST").isCode(150))
			return null;

		BufferedReader reader = new BufferedReader(new InputStreamReader(data.getInputStream(),
				Charset.forName(CHARSET)));
		while (reader.ready())
			list.add(reader.readLine());
		data.close();

		if (!getMessage().isCode(226))
			return null;

		return list.toArray(new String[list.size()]);
	}

	public ListEntry[] mlsd() throws IOException {
		Socket data = openDataPort();
		List<ListEntry> entries = new ArrayList<ListEntry>();

		if (!send("MLSD").isCode(150))
			return null;

		BufferedReader reader = new BufferedReader(new InputStreamReader(data.getInputStream(),
				Charset.forName("UTF-8")));
		while (reader.ready())
			entries.add(new ListEntry(reader.readLine()));
		data.close();

		if (!getMessage().isCode(226))
			return null;

		return entries.toArray(new ListEntry[entries.size()]);
	}

	public boolean retrieve(String filename) throws IOException {
		return retrieve(filename, new File("."));
	}

	public boolean retrieve(String filename, File dir) throws IOException {
		if (!dir.isDirectory())
			return false;

		Socket data = openDataPort();
		if (!send("RETR " + filename).isCode(150))
			return false;

		File file = new File(dir, filename);
		copy(data.getInputStream(), new FileOutputStream(file));

		if (!getMessage().isCode(226))
			return false;

		return true;
	}

	public boolean store(File file) throws IOException {
		if (!file.exists())
			return false;

		Socket data = openDataPort();
		if (!send("STOR " + file.getName()).isCode(150))
			return false;

		copy(new FileInputStream(file), data.getOutputStream());

		if (!getMessage().isCode(226))
			return false;

		return true;
	}

	public boolean rename(String from, String to) throws IOException {
		if (!send("RNFR " + from).isCode(350))
			return false;
		if (!send("RNTO " + to).isCode(250))
			return false;
		return true;
	}

	public boolean delete(String file) throws IOException {
		return send("DELE " + file).isCode(250);
	}

	public boolean makeDirectory(String dir) throws IOException {
		return send("MKD " + dir).isCode(257);
	}

	public boolean removeDirectory(String dir) throws IOException {
		return send("RMD " + dir).isCode(250);
	}

	private FtpServerMessage getMessage() throws IOException {
		if (!socket.isConnected())
			throw new IOException("already closed");

		FtpServerMessage msg = new FtpServerMessage(reader);
		messageLog.add(msg);
		if (msg.getCode() == null || msg.isCode(421))
			throw new IOException(msg.toString());

		return msg;
	}

	private FtpServerMessage send(String msg) throws IOException {
		if (!socket.isConnected())
			throw new IOException("already closed");
		if (messageLog.lastElement().isCode(421))
			throw new IOException(messageLog.lastElement().toString());

		writer.println(msg);
		writer.flush();
		return getMessage();
	}

	private Socket openDataPort() throws IOException {
		return passive();
	}

	private Socket passive() throws IOException {
		FtpServerMessage msg = send("PASV");
		if (!msg.isCode(227))
			return null;

		String[] addr = extract(msg.getMessages()[0], "(", ")").split(",");
		InetAddress host = InetAddress.getByAddress(new byte[] { new Byte(addr[0]), new Byte(addr[1]),
				new Byte(addr[2]), new Byte(addr[3]) });
		int port = (Integer.parseInt(addr[4]) << 8) + Integer.parseInt(addr[5]);
		Socket pasv = new Socket(host, port);

		return pasv;
	}

	public void close() {
		try {
			send("QUIT");
			socket.close();
		} catch (IOException e) {
		}
	}

	private String extract(String str, String bracket) {
		return extract(str, bracket, bracket);
	}

	private String extract(String str, String startBracket, String endBracket) {
		int begin = str.indexOf(startBracket) + 1;
		int end = str.indexOf(endBracket, begin);
		return str.substring(begin, end);
	}

	private void copy(InputStream is, OutputStream os) {
		try {
			byte[] buf = new byte[BUF_SIZE];
			while (true) {
				int len = is.read(buf, 0, buf.length);
				if (len == -1)
					break;
				os.write(buf, 0, len);
			}
		} catch (IOException e) {
			logger.error("kraken-ftp: " + e.getMessage());
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				logger.error("kraken-ftp: close failed", e);
			}
			try {
				is.close();
			} catch (IOException e) {
				logger.error("kraken-ftp: close failed", e);
			}
		}
	}
}

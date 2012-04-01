package org.krakenapps.termio;

import java.io.IOException;
import java.io.OutputStream;

public class TerminalOutputStream extends OutputStream {
	private OutputStream out;

	public TerminalOutputStream(OutputStream out) {
		this.out = out;
	}

	public void print(String s) throws IOException {
		out.write(s.getBytes());
		out.flush();
	}

	public void println(String line) throws IOException {
		out.write((line + "\r\n").getBytes());
		out.flush();
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

}

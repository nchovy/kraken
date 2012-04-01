package org.krakenapps.honey.sshd.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.krakenapps.honey.sshd.HoneyFileSystem;
import org.krakenapps.honey.sshd.HoneyPath;

public class HoneyFileSystemImpl implements HoneyFileSystem {

	private File baseDir;

	public HoneyFileSystemImpl(File baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public List<HoneyPath> listFiles(HoneyPath path) throws IOException {
		File f = new File(baseDir, path.getPath());
		if (!f.getAbsolutePath().startsWith(baseDir.getAbsolutePath()))
			throw new IOException("invalid path: " + path);

		if (!f.exists())
			throw new IOException("directory not found: " + path);

		if (!f.isDirectory())
			throw new IOException("path is not a directory: " + path);

		List<HoneyPath> paths = new ArrayList<HoneyPath>();
		for (File c : f.listFiles()) {
			paths.add(new HoneyPath(path + "/" + c.getName()));
		}
		return paths;
	}

	@Override
	public void mkdirs(HoneyPath path) throws IOException {
		throw new IOException("not implemented yet");
	}

	@Override
	public OutputStream create(HoneyPath path) throws IOException {
		throw new IOException("not implemented yet");
	}

	@Override
	public InputStream open(HoneyPath path) throws IOException {
		File f = new File(baseDir, path.getPath());
		if (!f.getAbsolutePath().startsWith(baseDir.getAbsolutePath()))
			throw new IOException("invalid path: " + path);

		return null;
	}

	@Override
	public boolean rename(HoneyPath src, HoneyPath dst) throws IOException {
		throw new IOException("not implemented yet");
	}

	@Override
	public void delete(HoneyPath path) throws IOException {
		throw new IOException("not implemented yet");
	}
}

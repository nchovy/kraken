/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.server.SshFile;
import org.apache.sshd.server.filesystem.NativeSshFile;

public class SshChrootedFile implements SshFile {
	private String basePath;
	private SshFile f;

	protected SshChrootedFile(String basePath, SshFile f) {
		this.basePath = basePath;
		this.f = f;
	}

	public NativeSshFile getNativeSshFile() {
		return (NativeSshFile) f;
	}

	@Override
	public String getAbsolutePath() {
		String path = f.getAbsolutePath().substring(basePath.length());
		if (path.isEmpty())
			return "/";
		return path;
	}

	@Override
	public boolean move(SshFile destination) {
		SshChrootedFile chrooted = (SshChrootedFile) destination;
		return f.move(chrooted.getNativeSshFile());
	}

	@Override
	public List<SshFile> listSshFiles() {
		ArrayList<SshFile> chrooted = new ArrayList<SshFile>();
		for (SshFile c : f.listSshFiles())
			chrooted.add(new SshChrootedFile(basePath, c));
		return chrooted;
	}

	@Override
	public String getName() {
		return f.getName();
	}

	@Override
	public boolean isDirectory() {
		return f.isDirectory();
	}

	@Override
	public boolean isFile() {
		return f.isFile();
	}

	@Override
	public boolean doesExist() {
		return f.doesExist();
	}

	@Override
	public boolean isReadable() {
		return f.isReadable();
	}

	@Override
	public boolean isWritable() {
		return f.isWritable();
	}

	@Override
	public boolean isRemovable() {
		return f.isRemovable();
	}

	@Override
	public SshFile getParentFile() {
		return f.getParentFile();
	}

	@Override
	public long getLastModified() {
		return f.getLastModified();
	}

	@Override
	public boolean setLastModified(long time) {
		return f.setLastModified(time);
	}

	@Override
	public long getSize() {
		return f.getSize();
	}

	@Override
	public boolean mkdir() {
		return f.mkdir();
	}

	@Override
	public boolean delete() {
		return f.delete();
	}

	@Override
	public void truncate() throws IOException {
		f.truncate();
	}

	@Override
	public OutputStream createOutputStream(long offset) throws IOException {
		return f.createOutputStream(offset);
	}

	@Override
	public InputStream createInputStream(long offset) throws IOException {
		return f.createInputStream(offset);
	}

	@Override
	public void handleClose() throws IOException {
		f.handleClose();
	}

	@Override
	public boolean create() throws IOException {
		return f.create();
	}

	@Override
	public String getOwner() {
		return f.getOwner();
	}

	@Override
	public boolean isExecutable() {
		return f.isExecutable();
	}

}

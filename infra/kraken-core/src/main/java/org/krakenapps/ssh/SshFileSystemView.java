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

import java.io.File;

import org.apache.sshd.common.Session;
import org.apache.sshd.server.SshFile;
import org.apache.sshd.server.filesystem.NativeFileSystemView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshFileSystemView extends NativeFileSystemView {
	private final Logger logger = LoggerFactory.getLogger(SshFileSystemView.class);
	private String basePath;

	public SshFileSystemView(Session session) {
		super(session.getUsername());
		File f = new File(System.getProperty("kraken.home.dir"), session.getUsername());
		if (f.mkdirs())
			logger.info("kraken core: created ssh user directory [{}]", f.getAbsolutePath());

		basePath = f.getAbsolutePath();
		if (File.separatorChar == '\\') {
			basePath = basePath.replace('\\', '/');
			basePath = "/" + basePath;
		}
	}

	@Override
	public SshFile getFile(String file) {
		return new SshChrootedFile(basePath, super.getFile(basePath + "/" + file));
	}
}

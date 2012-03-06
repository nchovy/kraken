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
package org.krakenapps.http.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.krakenapps.http.UploadCallback;
import org.krakenapps.http.FileUploadService;
import org.krakenapps.http.UploadToken;
import org.krakenapps.http.UploadedFile;
import org.krakenapps.http.internal.util.MimeTypes;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUploadServlet extends HttpServlet implements FileUploadService {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(FileUploadServlet.class.getName());
	private static final String BASE_PATH = "base_path";

	// upload config node
	private Preferences prefs;
	private File baseDir;
	private File tempDir;

	private ConcurrentMap<String, AtomicInteger> counters;
	private ConcurrentMap<String, UploadItem> items;
	private ConcurrentMap<String, Collection<String>> downloadTokens;

	public FileUploadServlet(BundleContext bc) {
		ServiceReference ref = bc.getServiceReference(PreferencesService.class.getName());
		if (ref == null)
			throw new IllegalStateException("osgi preferences service is required");

		PreferencesService prefsvc = (PreferencesService) bc.getService(ref);
		prefs = prefsvc.getSystemPreferences().node("upload");
	}

	public void start() {
		baseDir = new File(prefs.get(BASE_PATH,
				new File(System.getProperty("kraken.data.dir"), "kraken-http/upload/").getAbsolutePath()));
		tempDir = new File(prefs.get(BASE_PATH,
				new File(System.getProperty("kraken.data.dir"), "kraken-http/upload/").getAbsolutePath()));
		baseDir.mkdirs();
		tempDir.mkdirs();

		// load counters per space
		counters = new ConcurrentHashMap<String, AtomicInteger>();
		items = new ConcurrentHashMap<String, UploadItem>();
		downloadTokens = new ConcurrentHashMap<String, Collection<String>>();

		try {
			for (String spaceId : prefs.childrenNames()) {
				Preferences node = prefs.node(spaceId);

				int max = 0;
				for (String resourceId : node.childrenNames()) {
					int id = Integer.parseInt(resourceId);
					if (id > max)
						max = id;
				}

				counters.put(spaceId, new AtomicInteger(max));
			}
		} catch (BackingStoreException e) {
			logger.warn("kraken http: failed to load upload space configurations", e);
		}
	}

	public void stop() {
		// nothing to do here
	}

	@Override
	public File getBaseDirectory() {
		return baseDir;
	}

	@Override
	public void setBaseDirectory(File dir) {
		if (dir == null)
			throw new IllegalArgumentException("upload dir must be not null");

		dir.mkdirs();

		prefs.put(BASE_PATH, dir.getAbsolutePath());

		baseDir = dir;
	}

	@Override
	public Collection<UploadedFile> getFiles(String spaceId) {
		Collection<UploadedFile> files = new ArrayList<UploadedFile>();
		try {
			if (!prefs.nodeExists(spaceId))
				return files;

			Preferences space = prefs.node(spaceId);
			for (String resourceId : space.childrenNames()) {
				Preferences node = space.node(resourceId);

				int id = Integer.parseInt(resourceId);
				String fileName = node.get("filename", null);
				long fileSize = node.getLong("filesize", 0);
				File file = new File(node.get("filepath", ""));

				files.add(new UploadedFileImpl(id, fileName, fileSize, file));
			}

			return files;
		} catch (BackingStoreException e) {
			logger.warn("kraken http: cannot retrieve file names of space", e);
		}
		return null;
	}

	@Override
	public int setUploadToken(UploadToken token, UploadCallback callback) {
		if (token == null)
			throw new IllegalArgumentException("upload token must be not null");

		String spaceId = token.getSpaceId();

		// add new counter if space does not exist
		counters.putIfAbsent(spaceId, new AtomicInteger(0));

		// get new resource id
		AtomicInteger counter = counters.get(spaceId);
		int nextId = counter.incrementAndGet();

		// set upload item on waiting table
		UploadItem old = items.putIfAbsent(token.getToken(), new UploadItem(token, nextId, callback));
		if (old != null)
			throw new IllegalStateException("duplicated http upload token: " + token.getToken());

		return nextId;
	}

	@Override
	public void removeDownloadToken(String token) {
		if (token != null)
			downloadTokens.remove(token);
	}

	@Override
	public void setDownloadToken(String token, Collection<String> spaces) {
		Collection<String> old = downloadTokens.putIfAbsent(token, spaces);
		if (old != null)
			throw new IllegalStateException("duplicated http download token: " + token);
	}

	@Override
	public void writeFile(String token, InputStream is) throws IOException {
		FileOutputStream os = null;
		long totalReadBytes = 0;

		// remove token from waiting table
		UploadItem item = items.remove(token);
		if (item == null)
			throw new IOException("upload token not found: " + token);

		logger.trace("kraken-http: new upload post for token {}, resouce id {}", token, item.resourceId);

		try {
			File temp = File.createTempFile("krakenhttp-", null, tempDir);
			os = new FileOutputStream(temp);
			byte[] buffer = new byte[8096];

			while (true) {
				int readBytes = is.read(buffer);
				if (readBytes <= 0)
					break;

				totalReadBytes += readBytes;
				os.write(buffer, 0, readBytes);
			}

			os.close();
			os = null;

			// check file size
			if (totalReadBytes == item.token.getFileSize()) {
				saveFile(temp, item);
			} else {
				// delete
				temp.delete();

				// failure callback
				try {
					item.callback.onUploadFile(item.token, null);
					logger.info("kraken http: upload failure {} {}", item.token.getSpaceId(), item.token.getFileName());
				} catch (Exception e) {
					logger.warn("kraken http: upload callback should not throw any exception", e);
				}
			}
		} catch (IOException e) {
			throw e;
		}
	}

	private void saveFile(File temp, UploadItem item) throws IOException {
		// callback
		UploadToken token = item.token;

		try {
			// build path
			File spaceDir = new File(baseDir, token.getSpaceId());
			spaceDir.mkdirs();

			// move file
			File dest = new File(spaceDir, Integer.toString(item.resourceId));
			logger.trace("kraken-http: move {} to {}", temp.getAbsolutePath(), dest.getAbsolutePath());
			if (!temp.renameTo(dest)) {
				throw new IOException(String.format("kraken-http: move [%s] to [%s] failed", temp.getAbsolutePath(),
						dest.getAbsolutePath()));
			}

			// save properties
			Preferences space = prefs.node(token.getSpaceId());
			Preferences node = space.node(Integer.toString(item.resourceId));
			node.put("filename", token.getFileName());
			node.putLong("filesize", token.getFileSize());
			node.put("filepath", dest.getAbsolutePath());

			space.flush();
			space.sync();

			// fire callbacks
			if (item.callback != null)
				item.callback.onUploadFile(token, item.resourceId);
		} catch (BackingStoreException e) {
			logger.warn("kraken-http: cannot save upload properties", e);
		}

		logger.trace("kraken-http: user file uploaded, space {}, name {}", token.getSpaceId(), token.getFileName());
	}

	@Override
	public UploadedFile getFile(String token, String spaceId, int resourceId) throws IOException {
		try {
			Collection<String> spaces = downloadTokens.get(token);
			if (spaces == null)
				throw new IllegalStateException("download token not found: " + token);

			if (!spaces.contains(spaceId))
				throw new SecurityException("access denied for token [" + token + "], space [" + spaceId + "]");

			if (!prefs.nodeExists(spaceId))
				throw new IllegalStateException("space not found: " + spaceId);

			Preferences space = prefs.node(spaceId);
			String id = Integer.toString(resourceId);

			if (!space.nodeExists(id))
				throw new IllegalStateException("resource not found: " + resourceId);

			Preferences p = space.node(id);
			String fileName = p.get("filename", null);
			long fileSize = p.getLong("filesize", 0);
			File file = new File(p.get("filepath", null));
			if (!file.exists())
				throw new IOException("file not found: id " + resourceId + ", path: " + file.getAbsolutePath());

			return new UploadedFileImpl(resourceId, fileName, fileSize, file);
		} catch (BackingStoreException e) {
			throw new IOException("cannot open upload metadata", e);
		}
	}

	@Override
	public void deleteFile(String spaceId, int resourceId) {
		try {
			// check and remove file metadata
			String id = Integer.toString(resourceId);
			if (!prefs.nodeExists(spaceId))
				throw new IllegalStateException("space not found: " + spaceId);

			Preferences space = prefs.node(spaceId);
			if (!space.nodeExists(id))
				throw new IllegalStateException("resource not found: " + resourceId);

			Preferences p = space.node(id);
			p.removeNode();

			// sync
			space.flush();
			space.sync();

			// remove physical file
			File spaceDir = new File(baseDir, spaceId);
			spaceDir.mkdirs();

			File f = new File(spaceDir, Integer.toString(resourceId));
			if (!f.delete())
				throw new IllegalStateException(String.format(
						"failed to delete uploaded file: space %s, resource %d, path %s", spaceId, resourceId,
						f.getAbsolutePath()));
		} catch (BackingStoreException e) {
			logger.error("kraken-http: delete file failed", e);
		}
	}

	private static class UploadItem {
		public UploadToken token;
		public int resourceId;
		public UploadCallback callback;

		public UploadItem(UploadToken token, int resourceId, UploadCallback callback) {
			this.token = token;
			this.resourceId = resourceId;
			this.callback = callback;
		}
	}

	@Override
	public void log(String message, Throwable t) {
		logger.warn("kraken http: file upload error", t);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		UploadedFile f = null;
		FileInputStream is = null;
		ServletOutputStream os = null;
		String token = null;
		String spaceId = null;
		int resourceId = 0;

		try {
			token = getDownloadToken(req);
			spaceId = req.getParameter("space");
			resourceId = Integer.parseInt(req.getParameter("resource"));

			if (token == null)
				throw new IllegalStateException("download token not found");

			f = getFile(token, spaceId, resourceId);
			is = new FileInputStream(f.getFile());
			os = resp.getOutputStream();
			logger.trace("kraken http: open downstream for {}", f.getFile().getAbsolutePath());

			String mimeType = MimeTypes.instance().getByFile(f.getFileName());
			resp.setHeader("Content-Type", mimeType);

			String dispositionType = null;
			if (req.getParameter("force_download") != null)
				dispositionType = "attachment";
			else
				dispositionType = "inline";

			resp.setHeader("Content-Disposition", dispositionType + "; filename=\"" + f.getFileName() + "\"");
			resp.setStatus(200);
			resp.setContentLength((int) f.getFileSize());

			byte[] b = new byte[8096];

			while (true) {
				int readBytes = is.read(b);
				if (readBytes <= 0)
					break;

				os.write(b, 0, readBytes);
			}
		} catch (Exception e) {
			resp.setStatus(500);
			logger.warn("kraken-http: cannot download space " + spaceId + ", id " + resourceId);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
		}
	}

	private String getDownloadToken(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				logger.trace("kraken-http: checking all cookie for download, {} = {}", cookie.getName(),
						cookie.getValue());
				if (cookie.getName().equals("kraken_session"))
					return cookie.getValue();
			}
		}

		return req.getParameter("session");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		String token = req.getHeader("X-Upload-Token");
		if (token == null) {
			logger.warn("kraken-http: upload token header not found for [{}:{}] stream", req.getRemoteAddr(),
					req.getRemotePort());
			return;
		}

		InputStream is = null;
		try {
			is = req.getInputStream();
			writeFile(token, is);
			resp.setStatus(200);
		} catch (Exception e) {
			resp.setStatus(500);
			logger.warn("kraken-http: upload post failed", e);
		} finally {
			if (is == null)
				return;

			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	public static class UploadedFileImpl implements UploadedFile {
		private int resourceId;
		private String fileName;
		private long fileSize;
		private File file;

		public UploadedFileImpl(int resourceId, String fileName, long fileSize, File file) {
			this.resourceId = resourceId;
			this.fileName = fileName;
			this.fileSize = fileSize;
			this.file = file;
		}

		@Override
		public int getResourceId() {
			return resourceId;
		}

		@Override
		public String getFileName() {
			return fileName;
		}

		@Override
		public long getFileSize() {
			return fileSize;
		}

		@Override
		public File getFile() {
			return file;
		}

		@Override
		public String toString() {
			return String.format("resource id [%d], filename [%s], size [%d], path [%s]", resourceId, fileName,
					fileSize, file.getAbsolutePath());
		}

	}
}

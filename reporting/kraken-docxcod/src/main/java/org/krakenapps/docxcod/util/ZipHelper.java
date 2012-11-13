package org.krakenapps.docxcod.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipHelper {

	private static final int BUFFER_SIZE = 8192;

	private static Logger logger = LoggerFactory.getLogger("ZipHelper");

	public static int archive(ZipOutputStream os, List<File> files, File baseDir) {
		BufferedInputStream bis = null;
		try {
			for (File f : files) {
				if (f.isDirectory())
					continue;
				String filePath = f.getAbsolutePath();
				// zip 안에 들어갈 상대경로를 추출
				if (!filePath.startsWith(baseDir.getAbsolutePath())) {
					System.err.println("not in basedir. omitted: " + f);
					continue;
				}
//				String zipEntryName = filePath.substring(baseDir.getAbsolutePath().length() + 1, filePath.length());
				String zipEntryName = extractSubPath(f, baseDir);

				try {
					ZipEntry zEntry = new ZipEntry(zipEntryName);
					zEntry.setTime(f.lastModified());
					os.putNextEntry(zEntry);

					bis = new BufferedInputStream(new FileInputStream(f));

					byte[] buffer = new byte[BUFFER_SIZE];
					int cnt = 0;
					while ((cnt = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
						os.write(buffer, 0, cnt);
					}
					os.closeEntry();
					bis.close();
				} catch (FileNotFoundException e) {
					logger.warn("target file not found", e);
					continue;
				} catch (IOException e) {
					logger.warn("exception while reading file", e);
					continue;
				}
			}
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
					// ignore
				}
			}
		}
		return 0;
	}

	public static int extract(InputStream is, File targetDir) throws FileNotFoundException {
		ZipInputStream zipIs = new ZipInputStream(is);

		if (!targetDir.exists())
			throw new FileNotFoundException("target directory does not exist. :" + targetDir);

		try {
			ZipEntry nextEntry = zipIs.getNextEntry();
			int extractedCount = 0;
			while (nextEntry != null) {
				String filename = nextEntry.getName();
				String dir = filename;

				if (dir.lastIndexOf("/") != -1) {
					dir = dir.substring(0, dir.lastIndexOf("/"));
					File parentDir = new File(targetDir, dir);
					parentDir.mkdirs();
				} else if (dir.lastIndexOf("\\") != -1) {
					dir = dir.substring(0, dir.lastIndexOf("\\"));
					File parentDir = new File(targetDir, dir);
					parentDir.mkdirs();
				}

				File file = new File(targetDir, filename);
				FileOutputStream fileOutputStream = null;
				try {
					fileOutputStream = new FileOutputStream(file);

					int readCnt = 0;

					byte[] buf = new byte[BUFFER_SIZE];
					do {
						readCnt = zipIs.read(buf);
						if (readCnt == -1)
							break;
						else {
							fileOutputStream.write(buf, 0, readCnt);
						}
					} while (readCnt != 0);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (fileOutputStream != null)
						fileOutputStream.close();
					zipIs.closeEntry();
				}
				nextEntry = zipIs.getNextEntry();
				extractedCount++;
			}
			return extractedCount;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (zipIs != null)
				try {
					zipIs.close();
				} catch (IOException e) {
					e.printStackTrace();
					// ignore
				}
		}

		return -1;
	}

	public static void getFilesRecursivelyIn(File entry, List<File> files) {
		getFilesRecursivelyIn(entry, files, null);
	}
	
	public static void getFilesRecursivelyIn(File entry, List<File> files, FileFilter filter) {
		if (entry.exists()) {
			if (filter == null || filter.accept(entry))
				files.add(entry);
			if (entry.isDirectory()) {
				File[] fileList = entry.listFiles();
				for (int i = 0; i < fileList.length; ++i) {
					getFilesRecursivelyIn(fileList[i], files, filter);
				}
			}
		}
	}
	
	public static String extractSubPath(File pathname, File baseDir) {
		String src = pathname.getAbsolutePath();
		String base = baseDir.getAbsolutePath();

		if (src.startsWith(base)) {
			if (src.equals(base))
				return "";
			else
				return src.substring(base.length() + 1);
		} else
			return src;
	}


}

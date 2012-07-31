package org.krakenapps.docxcod;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.krakenapps.docxcod.util.ZipHelper;

public class RptTemplateProcessor {

	private static final int BUFFER_SIZE = 1024 * 8;

	private HashMap<String, Object> dataSource = new HashMap<String, Object>();
	private FileDocumentSource docSource;
	private Config docConfig;

	public RptTemplateProcessor(Config defaultConfig) {
		docConfig = defaultConfig;
		// 작업 디렉토리를 로컬에 저장.
	}

	public void setDataSource(HashMap<String, Object> rootObj) {
		dataSource = rootObj;
	}

	public void setDocumentSource(FileDocumentSource fileDocumentSource) {
		docSource = fileDocumentSource;
	}

	public RptOutput generateOutput() throws Exception {
		// 일단 전제 조건들을 체크 (위의 함수들이 제대로 호출되었는가)
		if (docSource == null)
			return null;

		// 체크하고 나서 documentSource 로부터 docx 를 읽고 config.workingDir 아래에 파일 압축을 풀고
		// workingDir 아래의 docSource.hashCode() 디렉토리에 docx 압축을 푼다.
		unzipDocument();

		// 압축을 푼 파일을 적절히 바꾸고
		// processDocument();

		// 적절히 바뀐 파일들을 다시 압축을 하고 압축 푼 파일을 기반으로 RptOutput 객체를 만든다.
		RptOutput output = makeRptOutput();

		// cleanUp 함수를 부른다.
		cleanUp();

		// 만들어둔 RptOutput 객체를 리턴.
		return output;
	}

	private void unzipDocument() {
		InputStream is;
		try {
			is = docSource.getInputStream();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("file open failed");
			return;
		}

		File baseDir = new File(docConfig.workingDir, Integer.toString(docSource.hashCode()));
		baseDir.mkdirs();

		try {
			ZipHelper.extract(is, baseDir);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	//압축하기	
	private RptOutput makeRptOutput() throws Exception {
		RptOutput output = new RptOutput();
		File outputFile = zipDocument();
		if (outputFile != null) {
			output.setFile(outputFile);
			return output;
		} else
			return null;
	}

	public File zipDocument() throws Exception {
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		File baseDir = new File(Integer.toString(docSource.hashCode()));
		try {
			File outputFile = getDocxOutputFile();
			fos = new FileOutputStream(outputFile);
			zos = new ZipOutputStream(fos);

			
			List<File> files = new ArrayList<File>();
			ZipHelper.getFilesRecursivelyIn(baseDir, files);
			ZipHelper.archive(zos, files, baseDir);

//			zipDir(baseDir, Integer.toString(docSource.hashCode()), zos);

			zos.finish();
			return outputFile;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (zos != null) {
				zos.close();
			}
			if (fos != null)
				fos.close();
		}

		return null;
	}

	private File getDocxOutputFile() throws IOException {
		return new File(docConfig.workingDir, File.createTempFile("docxcod", ".docx").getName());
	}

	private static void zipDir(File entry, String basePath, ZipOutputStream zos) throws Exception {

		if (entry.isDirectory()) {
			File[] fileList = entry.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				zipDir(fileList[i], basePath, zos);
			}
		} else {
			BufferedInputStream bis = null;
			try {
				String filePath = entry.getPath();
				// zip 안에 들어갈 상대경로를 추출
				String zipEntryName = filePath.substring(basePath.length() + 1, filePath.length());

				bis = new BufferedInputStream(new FileInputStream(entry));
				ZipEntry zEntry = new ZipEntry(zipEntryName);
				zEntry.setTime(entry.lastModified());
				zos.putNextEntry(zEntry);

				byte[] buffer = new byte[BUFFER_SIZE];
				int cnt = 0;
				while ((cnt = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
					zos.write(buffer, 0, cnt);
				}
				zos.closeEntry();
			} finally {
				if (bis != null) {
					bis.close();
				}
			}
		}
	}

	private void cleanUp() {
		File baseDirName = new File(docConfig.workingDir, Integer.toString(docSource.hashCode()));

		System.err.println(baseDirName);

		if (!baseDirName.exists())
			return;

		deleteDir(baseDirName);
	}

	public static void deleteDir(File file) {
		if (file.isDirectory()) {
			if (file.listFiles().length != 0) {
				File[] fileList = file.listFiles();
				for (int i = 0; i < fileList.length; i++) {
					deleteDir(fileList[i]);
					file.delete();
				}
			} else {
				file.delete();
			}
		} else {
			file.delete();
		}
	}

}

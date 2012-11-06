package org.krakenapps.docxcod;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagicNodeUnwrapper implements OOXMLProcessor {
	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	
	private static Pattern MAGICNODE_PTRN_DRCTV = Pattern.compile("<KMagicNode><!\\[CDATA\\[(#(.*)|\\/#(.*)|@(.*))\\]\\]><\\/KMagicNode>");
	private static Pattern MAGICNODE_PTRN_EXPR = Pattern.compile("<KMagicNode><!\\[CDATA\\[(.*)\\]\\]><\\/KMagicNode>");

	@Override
	public void process(OOXMLPackage pkg, Map<String, Object> rootMap) {
		/*
		 * augmented directive is processed with standard XML API. Because
		 * Freemarker directive is not legal XML element, contents of augmented
		 * directive should be wrapped to legal XML element, "KMagicNode", in
		 * extractMergeField method.
		 * 
		 * In this method, all KMagicNode element will be translated into
		 * Freemarker directive without XML API.
		 */
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		FileInputStream fis = null;

		try {
			File docXml = new File(pkg.getDataDir(), "word/document.xml");
			File newDocXml = new File(docXml + ".new");

			fis = new FileInputStream(docXml);
			String xmlString = new Scanner(fis, "UTF-8").useDelimiter("\\A").next();
			fis.close();

			Matcher matcher = MAGICNODE_PTRN_DRCTV.matcher(xmlString);
			xmlString = matcher.replaceAll("<$1>");
			matcher = MAGICNODE_PTRN_EXPR.matcher(xmlString);
			xmlString = matcher.replaceAll("$1");

			InputStream in = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
			FileOutputStream fos = new FileOutputStream(newDocXml);

			bis = new BufferedInputStream(in);
			bos = new BufferedOutputStream(fos);

			int len = 0;
			byte[] buf = new byte[1024];
			while ((len = bis.read(buf, 0, 1024)) != -1) {
				bos.write(buf, 0, len);
			}

			bis.close();
			bos.close();
			bis = null;
			bos = null;

			logger.trace("unwrapMagicNode: rename {} to {}", newDocXml, docXml);
			boolean deleteResult = docXml.delete();
			if (deleteResult) {
				logger.trace("unwrapMagicNode: deleting old file success: {}", docXml);
				newDocXml.renameTo(docXml);
			} else {
				logger.error("unwrapMagicNode: deleting old file failed: {}", docXml);
			}

		} catch (Exception e) {
			logger.warn("Exception in unwrapMagicNode", e);
		} finally {
			safeClose(fis);
			safeClose(bis);
			safeClose(bos);
		}
	}

	private void safeClose(Closeable f) {
		if (f == null)
			return;
		try {
			f.close();
		} catch (Exception e) {
			// ignore
		}
	}
}

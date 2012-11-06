package org.krakenapps.docxcod;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreeMarkerRunner implements OOXMLProcessor {
	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	private final Map<String, Object> data;

	public FreeMarkerRunner(Map<String, Object> data)
	{
		this.data = data;
	}

	@Override
	public void process(OOXMLPackage docx, Map<String, Object> rootMap) {
		// TODO Auto-generated method stub
		Configuration cfg = new Configuration();
		cfg.setObjectWrapper(new DefaultObjectWrapper());

		for (String s : docx.listParts("")) {
			InputStreamReader templateReader = null;
			
			PrintWriter writer = null;
			File sf = new File(docx.getDataDir(), s);
			File outf = new File(sf.getPath() + ".new");
			try {
				templateReader = new InputStreamReader(new FileInputStream(sf));
				writer = new PrintWriter(new FileOutputStream(outf));
				logger.info("process: try freemarker template processing: {}", sf);
				Template t = new Template(s, templateReader, cfg);
				t.process(data, writer);
				logger.trace("process: freemarker template processing completed");
				safeClose(writer);
				safeClose(templateReader);
				writer = null;
				templateReader = null;

				logger.trace("process: rename {} to {}", outf, sf);
				boolean deleteResult = sf.delete();
				if (deleteResult) {
					logger.trace("process: deleting old file success: {}", sf);
					outf.renameTo(sf);
				} else {
					logger.error("process: deleting old file failed: {}", sf);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TemplateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				safeClose(templateReader);
				safeClose(writer);
			}
		}
	}

	private void safeClose(Closeable o) {
		try {
			if (o != null)
				o.close();
		} catch (IOException e) {
			// ignore
		}
	}
}

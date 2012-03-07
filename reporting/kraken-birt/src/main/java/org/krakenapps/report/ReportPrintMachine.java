package org.krakenapps.report;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.birt.report.model.api.IResourceLocator;

public interface ReportPrintMachine {
	
	String getReportMachinePath();
	
	void setReportMachinePath(File file);

	boolean print(InputStream istream, Map<String, Object> reportParameters,
			BirtReportOutputType outputType, OutputStream outputStream, IResourceLocator resourceLocator);

	boolean print(InputStream istream, Map<String, Object> reportParameters,
			BirtReportOutputType outputType, OutputStream outputStream);
	
	boolean restart();

}

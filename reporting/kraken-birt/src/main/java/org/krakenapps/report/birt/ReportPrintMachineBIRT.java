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
package org.krakenapps.report.birt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.model.api.IResourceLocator;
import org.krakenapps.report.BirtReportOutputType;
import org.krakenapps.report.ReportPrintMachine;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;

@Component(name = "birt-report-print-machine")
@Provides
public class ReportPrintMachineBIRT implements ReportPrintMachine {
	private static final String DEFAULT_BIRT_PATH = System.getProperty("kraken.data.dir") + "/kraken-birt";
	private static final String DATA_SRC = "dataSrc";
	private static final String DATA_SRC_NAME = "dataSrc";
	private static final String BIRT_PATH_KEY = "home";

	@Requires
	private PreferencesService prefsvc;
	private IReportEngine engine = null;
	private EngineConfig config = new EngineConfig();
	private File homeDir = null;

	public ReportPrintMachineBIRT() {
	}

	public ReportPrintMachineBIRT(IReportEngine engine) {
		this.engine = engine;
	}

	@Override
	public String getReportMachinePath() {
		Preferences root = getRootPreference();
		return root.get(BIRT_PATH_KEY, DEFAULT_BIRT_PATH);
	}

	@Override
	public void setReportMachinePath(File file) {
		try {
			Preferences root = getRootPreference();
			root.put(BIRT_PATH_KEY, file.getAbsolutePath());
			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Validate
	public boolean setUp() {
		Preferences root = getRootPreference();
		String home = root.get(BIRT_PATH_KEY, DEFAULT_BIRT_PATH);
		homeDir = new File(home);
		homeDir.mkdirs();

		String enginePath = new File(homeDir, "engine").getAbsolutePath();
		String logPath = new File(homeDir, "log").getAbsolutePath();
		String resourcePath = new File(homeDir, "resources").getAbsolutePath();

		System.out.println("enginePath: " + enginePath);

		return setUp(enginePath, logPath, resourcePath);
	}

	public boolean setUp(String enginePath, String logPath, String resourcePath) {
		config.setEngineHome(enginePath);
		config.setLogConfig(logPath, Level.FINE);
		config.setResourcePath(resourcePath);

		try {
			Platform.startup(config);
			IReportEngineFactory factory = (IReportEngineFactory) Platform
					.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
			engine = factory.createReportEngine(config);
			engine.changeLogLevel(Level.ALL);

			return true;

		} catch (BirtException e) {
			e.printStackTrace();
		}

		return false;
	}

	private Preferences getRootPreference() {
		Preferences root = prefsvc.getSystemPreferences().node("/kraken-birt");
		return root;
	}

	@Invalidate
	public void tearDown() {
		if (engine != null)
			engine.destroy();
		Platform.shutdown();
	}

	@Override
	public boolean restart() {
		tearDown();
		return setUp();
	}

	@Override
	public boolean print(InputStream istream, Map<String, Object> reportParameters, BirtReportOutputType outputType,
			OutputStream outputStream) {
		return print(istream, reportParameters, outputType, outputStream, null);
	}

	@Override
	public boolean print(InputStream istream, Map<String, Object> reportParameters, BirtReportOutputType outputType,
			OutputStream outputStream, IResourceLocator resourceLocator) {

		IRenderOption option;
		switch (outputType) {
		case DOC:
			option = new PDFRenderOption();
			option.setOutputFormat("doc");
			break;
		case PDF:
			option = new PDFRenderOption();
			option.setOutputFormat("pdf");
			break;
		default:
			System.err.println("Not supported output type.");
			return false;
		}
		option.setOutputStream(outputStream);
		option.setSupportedImageFormats("PNG;GIF;JPG;BMP;SWF;SVG");

		System.setProperty("java.awt.headless", "true");

		boolean result;

		try {

			synchronized (this) {
				IReportRunnable report;
				if (resourceLocator != null) {
					report = engine.openReportDesign(null, istream, resourceLocator);
				} else
					report = engine.openReportDesign(istream);

				IRunAndRenderTask task = engine.createRunAndRenderTask(report);
				if (task == null)
					throw new IllegalStateException(
							"task is null. check if engine libraries are installed in data/kraken-birt/engine");

				task.setRenderOption(option);
				task.setParameterValues(reportParameters);
				if (task.getParameterValue(DATA_SRC_NAME) == null) {
					task.setParameterValue(DATA_SRC_NAME, new File(homeDir, DATA_SRC).getAbsolutePath());
				}
				task.run();
				task.close();
				result = true;
			}

		} catch (EngineException e) {
			e.printStackTrace();
			result = false;
		} finally {
			try {
				outputStream.flush();
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public static void main(String[] args) throws IOException {
		ReportPrintMachineBIRT engine = new ReportPrintMachineBIRT();
		String enginePath = new File("D:\\kraken\\data\\kraken-birt\\engine").getAbsolutePath();
		String logPath = new File("C:\\temp").getAbsolutePath();

		engine.config.setEngineHome(enginePath);
		engine.config.setLogConfig(logPath, Level.FINE);

		try {
			Platform.startup(engine.config);
			IReportEngineFactory factory = (IReportEngineFactory) Platform
					.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
			engine.engine = factory.createReportEngine(engine.config);
			engine.engine.changeLogLevel(Level.ALL);

		} catch (BirtException e) {
			e.printStackTrace();
		}
		try {
			File templateFile = new File(
					"D:\\ReportingSoftwares\\EclipseBIRT-2.6.0\\workspace\\IGIMS_Reports\\daily_firewall_report.rptdesign");

			File baseDir = new File("D:\\ReportingSoftwares\\EclipseBIRT-2.6.0\\workspace\\IGIMS_Reports\\");

			OutputStream ostream = new BufferedOutputStream(new FileOutputStream(
					"D:\\ReportingSoftwares\\EclipseBIRT-2.6.0\\workspace\\IGIMS_Reports\\daily_firewall_report.doc"));
			Map<String, Object> reportParameters = new HashMap<String, Object>(1);
			reportParameters.put("baseDir", baseDir.getAbsolutePath());
			engine.print(new FileInputStream(templateFile), reportParameters, BirtReportOutputType.DOC, ostream, null);
		} finally {
			engine.tearDown();
		}
	}

}

/*
 * Copyright 2009 NCHOVY
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.IPojoFactory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.architecture.Architecture;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.http.BundleHttpContext;
import org.krakenapps.http.FilesystemHttpContext;
import org.krakenapps.http.HttpServiceManager;
import org.krakenapps.http.KrakenHttpService;
import org.krakenapps.http.ServletLifecycleManager;
import org.krakenapps.http.FileUploadService;
import org.krakenapps.http.UploadToken;
import org.krakenapps.http.UploadedFile;
import org.krakenapps.http.internal.context.ServletContextImpl;
import org.krakenapps.http.internal.handler.ServletHandler;
import org.krakenapps.http.internal.service.ResourceServlet;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpScript implements Script {
	private Logger logger = LoggerFactory.getLogger(HttpScript.class);
	private ScriptContext context;
	private BundleContext bc;
	private HttpServiceManager manager;
	private FileUploadService upload;

	public HttpScript(BundleContext bundleContext, HttpServiceManager manager, FileUploadService upload) {
		this.bc = bundleContext;
		this.manager = manager;
		this.upload = upload;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "set upload token", arguments = {
			@ScriptArgument(name = "token", type = "string", description = "upload token guid"),
			@ScriptArgument(name = "space id", type = "string", description = "the space id"),
			@ScriptArgument(name = "file name", type = "string", description = "original file name"),
			@ScriptArgument(name = "file size", type = "int", description = "original file size") })
	public void setUploadToken(String[] args) {
		UploadTokenImpl token = new UploadTokenImpl();
		token.token = args[0];
		token.spaceId = args[1];
		token.fileName = args[2];
		token.fileSize = Long.parseLong(args[3]);

		try {
			int resourceId = upload.setUploadToken(token, null);
			context.println("new resource id = " + resourceId);
		} catch (IllegalStateException e) {
			context.println("upload token already exists");
		}
	}

	@ScriptUsage(description = "set download token and associated spaces", arguments = {
			@ScriptArgument(name = "token", type = "string", description = "download token"),
			@ScriptArgument(name = "spaces", type = "string", description = "one or more space identifiers") })
	public void setDownloadToken(String[] args) {
		String token = args[0];
		List<String> spaces = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));

		try {
			upload.setDownloadToken(token, spaces);
			context.println("download token added");
		} catch (IllegalStateException e) {
			context.println("download token already exists");
		}
	}

	@ScriptUsage(description = "delete uploaded file", arguments = {
			@ScriptArgument(name = "space id", type = "string", description = "the space id"),
			@ScriptArgument(name = "resource id", type = "string", description = "the resource id") })
	public void deleteFile(String[] args) {
		try {
			String spaceId = args[0];
			int resourceId = Integer.parseInt(args[1]);

			upload.deleteFile(spaceId, resourceId);
			context.println("file deleted");
		} catch (Exception e) {
			context.println("error: " + e.getMessage());
		}
	}

	private static class UploadTokenImpl implements UploadToken {
		public String token;
		public String spaceId;
		public String fileName;
		public long fileSize;

		@Override
		public String getToken() {
			return token;
		}

		@Override
		public String getSpaceId() {
			return spaceId;
		}

		@Override
		public String getFileName() {
			return fileName;
		}

		@Override
		public long getFileSize() {
			return fileSize;
		}
	}

	@ScriptUsage(description = "list all upload files in space", arguments = { @ScriptArgument(name = "space id", type = "string", description = "space id") })
	public void listFiles(String[] args) {
		String spaceId = args[0];

		context.println("Uploaded Files");
		context.println("--------------------");
		for (UploadedFile f : upload.getFiles(spaceId)) {
			context.println(f.toString());
		}
	}

	@ScriptUsage(description = "list all iPOJO servlet factories")
	public void servletFactories(String[] args) {
		try {
			ServiceReference[] refs = bc.getServiceReferences(Factory.class.getName(), null);
			for (ServiceReference ref : refs) {
				Factory factory = (Factory) bc.getService(ref);
				if (isServletFactory(factory)) {
					context.println(ref.getProperty("factory.name").toString());
				}
			}

		} catch (InvalidSyntaxException e) {
			// ignore
		}
	}

	@ScriptUsage(description = "list all registered servlets. Usage: servlets [filter string]")
	public void servlets(String[] args) {
		String nameFilter = null;
		if (args.length > 0)
			nameFilter = args[0];

		try {
			ServiceReference[] refs = bc.getServiceReferences(Servlet.class.getName(), null);
			context.println("Servlet Components");
			context.println("==================");

			if (refs == null)
				return;

			for (ServiceReference ref : refs) {
				String instanceName = (String) ref.getProperty("instance.name");

				if (nameFilter != null) {
					if (!instanceName.contains(nameFilter))
						continue;
				}

				context.println(instanceName);
			}
		} catch (InvalidSyntaxException e) {
			// ignore
		}
	}

	private boolean isServletFactory(Factory factory) {

		String[] interfaces = factory.getComponentDescription().getprovidedServiceSpecification();
		for (String inf : interfaces) {
			if (inf.equals(Servlet.class.getName()))
				return true;
		}

		return false;
	}

	private KrakenHttpService findHttpService(String httpServiceName) {
		try {
			String serviceNameFilter = "(httpservice.name=" + httpServiceName + ")";
			String interfaceName = KrakenHttpService.class.getName();
			ServiceReference[] refs = bc.getServiceReferences(interfaceName, serviceNameFilter);

			if (refs == null) {
				context.printf("http service [%s] not found\n", httpServiceName);
				return null;
			}

			return (KrakenHttpService) bc.getService(refs[0]);
		} catch (InvalidSyntaxException e) {
			return null;
		}
	}

	@ScriptUsage(description = "register upload servlet", arguments = {
			@ScriptArgument(name = "http service name", type = "string", description = "the name of http service"),
			@ScriptArgument(name = "path", type = "string", description = "url path") })
	public void registerUploadServlet(String[] args) {
		try {
			String httpServiceName = args[0];
			String path = args[1];

			KrakenHttpService httpService = findHttpService(httpServiceName);
			if (httpService == null) {
				context.println("http service not found: " + httpServiceName);
				return;
			}

			ServiceReference ref = bc.getServiceReference(FileUploadService.class.getName());
			FileUploadServlet upload = (FileUploadServlet) bc.getService(ref);

			httpService.registerServlet(path, upload, null, null);
			context.println("upload servlet registered");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "create servlet and register it", arguments = {
			@ScriptArgument(name = "http service name"), @ScriptArgument(name = "servlet factory name"),
			@ScriptArgument(name = "servlet alias") })
	public void createServlet(String[] args) {
		ComponentInstance servletInstance = null;
		try {
			String httpServiceName = args[0];
			String factoryName = args[1];
			String alias = args[2];

			KrakenHttpService httpService = findHttpService(httpServiceName);
			if (httpService == null) {
				context.println("http service not found: " + httpServiceName);
				return;
			}

			String factoryNameFilter = "(factory.name=" + factoryName + ")";
			ServiceReference[] refs = bc.getServiceReferences(Factory.class.getName(), factoryNameFilter);
			if (refs == null) {
				context.println("factory not found");
				return;
			}

			Factory factory = (Factory) bc.getService(refs[0]);
			servletInstance = factory.createComponentInstance(null);
			servletInstance.start();

			Servlet servlet = (Servlet) ((InstanceManager) servletInstance).getPojoObject();

			// register servlet to http service
			httpService.registerServlet(alias, servlet, null, null);
			ServletLifecycleManager slm = getServletLifecycleManager();
			if (slm != null) {
				slm.registerServlet(httpService, alias, servletInstance);
			} else {
				context.println("warning: ServletLifecycleManager instance doesn't exists");
			}

			// to avoid exception handling routine
			servletInstance = null;
		} catch (InvalidSyntaxException e) {
			context.println("invalid ldap filter exception");
		} catch (UnacceptableConfiguration e) {
			context.println("iPOJO unacceptable configuration exception: " + e.toString());
		} catch (MissingHandlerException e) {
			context.println("iPOJO missing handler exception: " + e.toString());
		} catch (ConfigurationException e) {
			context.println("iPOJO configuration exception: " + e.toString());
		} catch (ServletException e) {
			context.println("servlet exception: " + e.toString());
		} catch (NamespaceException e) {
			context.println("namespace exception: " + e.toString());
		} finally {
			if (servletInstance != null) {
				System.err.println("trying to dispose orphaned servlet instance..(" + servletInstance.getInstanceName()
						+ ")");
				servletInstance.stop();
				servletInstance.dispose();
			}
		}
	}

	private ServletLifecycleManager getServletLifecycleManager() {
		ServiceReference svcRef = bc.getServiceReference(ServletLifecycleManager.class.getName());
		if (svcRef == null)
			return null;
		else
			return (ServletLifecycleManager) bc.getService(svcRef);
	}

	@ScriptUsage(description = "unregister servlet and remove it", arguments = {
			@ScriptArgument(name = "http service name", type = "string", description = "the name of the http service component instance"),
			@ScriptArgument(name = "servlet name", type = "string", description = "the name of servlet component instance") })
	public void removeServlet(String[] args) {
		String httpServiceName = args[0];
		String servletName = args[1];

		try {
			// find http service
			KrakenHttpService httpService = findHttpService(httpServiceName);
			if (httpService == null) {
				context.println("http service not found: " + httpServiceName);
				return;
			}

			// find servlet
			String instanceNameFilter = "(instance.name=" + servletName + ")";
			ServiceReference[] refs = bc.getServiceReferences(Servlet.class.getName(), instanceNameFilter);
			if (refs == null) {
				context.println("servlet not found: " + servletName);
				return;
			}

			Servlet servlet = (Servlet) bc.getService(refs[0]);

			// unregister servlet
			httpService.unregisterServlet(servlet);

			// remove servlet
			removeServletInstance(servletName);

		} catch (InvalidSyntaxException e) {
			// ignore
		}
	}

	private void removeServletInstance(String servletName) throws InvalidSyntaxException {
		ServiceReference[] archRefs = bc.getServiceReferences(Architecture.class.getName(), "(architecture.instance="
				+ servletName + ")");
		if (archRefs == null || archRefs.length == 0)
			return;

		Architecture arch = (Architecture) bc.getService(archRefs[0]);
		IPojoFactory iPojoFactory = arch.getInstanceDescription().getComponentDescription().getFactory();
		iPojoFactory.deleted(servletName);
	}

	@ScriptUsage(description = "list all http/https services")
	public void list(String[] args) {
		context.println("HTTP Service List");
		context.println("=================");

		for (String httpServiceName : manager.getHttpServiceList()) {
			context.println(httpServiceName + "'s contexts:");
			Map<String, String> config = manager.getConfig(httpServiceName);
			if (config != null) {
				context.println("\tConfigs:");
				for (Entry<String, String> entry : config.entrySet()) {
					context.printf("\t\t%s: %s\n", entry.getKey(), entry.getValue());
				}
			}
			KrakenHttpService httpService = findHttpService(httpServiceName);
			ServletHandler[] servlets = httpService.servlets();
			for (ServletHandler servletHandler : servlets) {
				context.println("\t" + servletHandler.toString());
				try {
					Servlet servlet = servletHandler.getServlet();
					if (servlet instanceof ResourceServlet) {
						ResourceServlet rs = (ResourceServlet) servlet;
						ServletContextImpl servletContext = (ServletContextImpl) rs.getServletContext();
						HttpContext httpContext = servletContext.getHttpContext();
						if (httpContext instanceof FilesystemHttpContext) {
							FilesystemHttpContext fhc = (FilesystemHttpContext) httpContext;
							context.println("\t\tbasePath = " + fhc.getResourceBase());
						}
					}
				} catch (Exception e) {
					context.println("exception occured. refer to logfile");
				}
			}
		}
	}

	@ScriptUsage(description = "open a new http service", arguments = { @ScriptArgument(name = "server name"),
			@ScriptArgument(name = "port", type = "integer") })
	public void open(String[] args) {
		String serverName = args[0];
		int port = Integer.parseInt(args[1]);

		if (port < 0 || port > 65535) {
			context.println("port out of range");
			return;
		}

		Map<String, String> config = new HashMap<String, String>();
		config.put("ssl", "false");
		config.put("port", Integer.toString(port));

		try {
			manager.openHttpService(serverName, config);
			context.println(port + " http service opened.");
		} catch (Exception e) {
			context.println(e.toString());
			logger.warn("kraken-http: open failed. " + e);
		}
	}

	@ScriptUsage(description = "open a new https service")
	public void openSsl(String[] args) {
		try {
			context.println(System.getProperty("user.dir"));
			context.print("Server ID: ");
			String serverName = context.readLine();

			context.print("Port: ");
			String port = context.readLine();
			int p = Integer.parseInt(port); // check number
			if (p < 0 || p > 65535) {
				context.println("port out of range");
				return;
			}

			context.print("Resource Base: ");
			String resourceBase = context.readLine();

			context.print("KeyStore: ");
			String keyStore = context.readLine();

			context.print("Truststore: ");
			String trustStore = context.readLine();

			context.turnEchoOff();
			context.print("Password: ");
			String password = context.readLine();

			context.print("KeyPassword: ");
			String keyPassword = context.readLine();

			context.print("TrustPassword: ");
			String trustPassword = context.readLine();

			Map<String, String> config = new HashMap<String, String>();
			config.put("ssl", "true");
			config.put("port", port);
			config.put("resourceBase", resourceBase);
			config.put("keyStore", System.getProperty("user.dir") + File.separatorChar + keyStore);
			config.put("trustStore", System.getProperty("user.dir") + File.separatorChar + trustStore);
			config.put("password", password);
			config.put("keyPassword", keyPassword);
			config.put("trustPassword", trustPassword);

			try {
				// create server with config
				manager.openHttpService(serverName, config);
				context.println(port + " https service opened.");
			} catch (Exception e) {
				context.println(e.toString());
				logger.warn("kraken-http: open failed. " + e);
			}
		} catch (InterruptedException e) {
		} catch (NumberFormatException e) {
		}
	}

	@ScriptUsage(description = "close the http server", arguments = { @ScriptArgument(name = "name", type = "string", description = "http server name") })
	public void close(String[] args) {
		try {
			String httpServiceName = args[0];
			manager.closeHttpService(httpServiceName);
			context.printf("[%s] service closed.\n", httpServiceName);
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "add filesystem resources", arguments = { @ScriptArgument(name = "http service name"),
			@ScriptArgument(name = "alias"), @ScriptArgument(name = "base path") })
	public void addFilesystemResources(String[] args) {
		String httpServiceName = args[0];
		String alias = args[1];
		String basePath = args[2];

		KrakenHttpService httpService = findHttpService(httpServiceName);
		if (httpService == null) {
			context.println("http service not found");
			return;
		}

		try {
			httpService.registerResources(alias, "", new FilesystemHttpContext(basePath));
			context.printf("base path [%s] is registered to alias [%s]\n", basePath, alias);
		} catch (NamespaceException e) {
			context.println("failed to register resources: " + e.toString());
		}
	}

	@ScriptUsage(description = "add bundle resources", arguments = {
			@ScriptArgument(name = "http service name", type = "string", description = "http service name"),
			@ScriptArgument(name = "alias", type = "string", description = "servlet alias"),
			@ScriptArgument(name = "resource name", type = "string", description = "resource path"),
			@ScriptArgument(name = "bundle id", type = "int") })
	public void addBundleResources(String[] args) {
		try {
			String httpServiceName = args[0];
			String alias = args[1];
			String resourceName = args[2];
			long bundleId = Long.parseLong(args[3]);
			Bundle bundle = bc.getBundle(bundleId);

			KrakenHttpService httpService = findHttpService(httpServiceName);
			if (httpService == null) {
				context.println("http service not found.");
				return;
			}

			httpService.registerResources(alias, resourceName, new BundleHttpContext(bundle));

			context.printf("resource name [%s] in bundle [%d] is registered to alias [%s]\n", resourceName, bundleId,
					alias);
		} catch (Exception e) {
			context.println(e.toString());
		}
	}
}

package org.krakenapps.log.api.msgbus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.log.api.LogNormalizerRegistry;
import org.krakenapps.log.api.LogParserFactory;
import org.krakenapps.log.api.LogParserFactoryRegistry;
import org.krakenapps.log.api.LogParserRegistry;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerFactoryRegistry;
import org.krakenapps.log.api.LoggerRegistry;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "logapi-logger-plugin")
@MsgbusPlugin
public class LoggerPlugin {
	@Requires
	private LoggerRegistry loggerRegistry;

	@Requires
	private LoggerFactoryRegistry loggerFactoryRegistry;

	@Requires
	private LogParserRegistry parserRegistry;

	@Requires
	private LogNormalizerRegistry normalizerRegistry;
	
	@Requires
	private LogParserFactoryRegistry parserFactoryRegistry;

	@MsgbusMethod
	public void getLoggerFactories(Request req, Response resp) {
		Locale locale = req.getSession().getLocale();
		resp.put("factories", Marshaler.marshal(loggerFactoryRegistry.getLoggerFactories(), locale));
	}

	@MsgbusMethod
	public void getParserFactories(Request req, Response resp) {
		Locale locale = req.getSession().getLocale();
		List<Object> l = new ArrayList<Object>();
		
		for (String name : parserFactoryRegistry.getNames()) {
			LogParserFactory f =  parserFactoryRegistry.get(name);
			l.add(Marshaler.marshal(f, locale));
		}
		
		resp.put("factories", l);
	}

	@MsgbusMethod
	public void getLoggers(Request req, Response resp) {
		resp.put("loggers", Marshaler.marshal(loggerRegistry.getLoggers()));
	}

	@MsgbusMethod
	public void getNormalizers(Request req, Response resp) {
		resp.put("normalizers", normalizerRegistry.getNames());
	}

	@MsgbusMethod
	public void getParsers(Request req, Response resp) {
		resp.put("parsers", parserRegistry.getNames());
	}

	@MsgbusMethod
	public void getFactoryOptions(Request req, Response resp) {
		String loggerFactoryName = req.getString("factory");
		Locale locale = req.getSession().getLocale();
		LoggerFactory loggerFactory = loggerFactoryRegistry.getLoggerFactory(loggerFactoryName);
		resp.put("options", Marshaler.marshal(loggerFactory.getConfigOptions(), locale));
	}

	@MsgbusMethod
	public void createLogger(Request req, Response resp) {
		String loggerFactoryName = req.getString("factory");
		String loggerNamespace = req.getString("namespace");
		String loggerName = req.getString("name");
		String description = req.getString("description");

		LoggerFactory loggerFactory = loggerFactoryRegistry.getLoggerFactory(loggerFactoryName);
		Properties config = new Properties();

		for (LoggerConfigOption opt : loggerFactory.getConfigOptions()) {
			String optName = opt.getName().replace('.', '_');
			String value = null;

			try {
				value = req.getString(optName);
				config.put(opt.getName(), opt.parse(value));
			} catch (NullPointerException e) {
				if (opt.isRequired())
					throw e;
			}
		}

		loggerFactory.newLogger(loggerNamespace, loggerName, description, config);
	}

	@MsgbusMethod
	public void removeLoggers(Request req, Response resp) {
		@SuppressWarnings("unchecked")
		List<String> loggers = (List<String>) req.get("loggers");

		for (String fullName : loggers) {
			Logger logger = loggerRegistry.getLogger(fullName);
			logger.stop();
			String[] tokens = fullName.split("\\\\");
			LoggerFactory factory = loggerFactoryRegistry.getLoggerFactory(logger.getFactoryNamespace(),
					logger.getFactoryName());
			factory.deleteLogger(tokens[0], tokens[1]);
		}
	}

	@MsgbusMethod
	public void startLogger(Request req, Response resp) {
		String fullName = req.getString("logger");
		int interval = req.getInteger("interval");
		Logger logger = loggerRegistry.getLogger(fullName);
		logger.start(interval);
	}

	@MsgbusMethod
	public void stopLogger(Request req, Response resp) {
		String fullName = req.getString("logger");
		int waitTime = req.getInteger("wait_time");

		Logger logger = loggerRegistry.getLogger(fullName);
		logger.stop(waitTime);
	}

}

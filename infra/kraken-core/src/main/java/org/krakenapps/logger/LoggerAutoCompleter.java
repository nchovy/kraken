package org.krakenapps.logger;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.api.AutoCompleteHelper;
import org.krakenapps.api.ScriptAutoCompletion;
import org.krakenapps.api.ScriptAutoCompletionHelper;
import org.krakenapps.api.ScriptSession;
import org.slf4j.impl.KrakenLoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

public class LoggerAutoCompleter implements ScriptAutoCompletionHelper {

	@Override
	public List<ScriptAutoCompletion> matches(ScriptSession session, String prefix) {
		KrakenLoggerFactory loggerFactory = (KrakenLoggerFactory) StaticLoggerBinder.getSingleton().getLoggerFactory();
		List<String> loggers = loggerFactory.getLoggerList();

		List<String> found = new ArrayList<String>();
		for (String logger : loggers)
			if (logger.startsWith(prefix))
				found.add(logger);

		String commonPrefix = AutoCompleteHelper.extractCommonPrefix(found);
		List<ScriptAutoCompletion> completions = new ArrayList<ScriptAutoCompletion>(loggers.size());
		for (String logger : found)
			completions.add(new ScriptAutoCompletion(logger, logger.substring(commonPrefix.length())));

		return completions;
	}
}

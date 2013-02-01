package org.krakenapps.logdb.query;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryCommandParser;
import org.krakenapps.logdb.LogQueryContext;
import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.LogQueryParserService;
import org.krakenapps.logdb.query.parser.QueryTokenizer;

@Component(name = "logdb-query-parser-service")
@Provides
public class LogQueryParserServiceImpl implements LogQueryParserService {

	private ConcurrentMap<String, LogQueryCommandParser> commandParsers = new ConcurrentHashMap<String, LogQueryCommandParser>();

	@Override
	public LogQuery parse(LogQueryContext context, String queryString) {
		List<LogQueryCommand> commands = new ArrayList<LogQueryCommand>();
		LogQuery lq = new LogQueryImpl(queryString, commands);

		for (String q : QueryTokenizer.parseCommands(queryString)) {
			q = q.trim();

			StringTokenizer tok = new StringTokenizer(q, " \t");
			String commandType = tok.nextToken();
			LogQueryCommandParser parser = commandParsers.get(commandType);
			if (parser == null)
				throw new LogQueryParseException("unsupported-command", -1, "command is [" + commandType + "]");

			LogQueryCommand cmd = parser.parse(context, q);
			cmd.setQueryString(q);
			cmd.setLogQuery(lq);
			commands.add(cmd);
		}

		if (commands.isEmpty())
			throw new IllegalArgumentException("empty query");

		boolean setReducer = false;
		for (int i = 0; i < commands.size(); i++) {
			LogQueryCommand command = commands.get(i);
			if (i < commands.size() - 1)
				command.setNextCommand(commands.get(i + 1));
			if (command.isReducer() && !setReducer && i > 0) {
				setReducer = true;
				commands.get(i - 1).setCallbackTimeline(true);
			}
		}
		if (!setReducer)
			commands.get(commands.size() - 1).setCallbackTimeline(true);

		return lq;
	}

	@Override
	public void addCommandParser(LogQueryCommandParser parser) {
		commandParsers.putIfAbsent(parser.getCommandName(), parser);
	}

	@Override
	public void removeCommandParser(LogQueryCommandParser parser) {
		commandParsers.remove(parser.getCommandName(), parser);
	}
}

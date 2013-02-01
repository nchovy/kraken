package org.krakenapps.logdb;

public interface LogQueryCommandParser {
	String getCommandName();

	LogQueryCommand parse(LogQueryContext context, String commandString);
}

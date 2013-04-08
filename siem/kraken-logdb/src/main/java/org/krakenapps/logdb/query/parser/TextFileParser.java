package org.krakenapps.logdb.query.parser;

import static org.krakenapps.bnf.Syntax.k;
import static org.krakenapps.bnf.Syntax.ref;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.log.api.LogParser;
import org.krakenapps.log.api.LogParserFactory;
import org.krakenapps.log.api.LogParserFactoryRegistry;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.TextFile;

public class TextFileParser implements LogQueryParser {

	private LogParserFactoryRegistry parserFactoryRegistry;

	public TextFileParser(LogParserFactoryRegistry parserFactoryRegistry) {
		this.parserFactoryRegistry = parserFactoryRegistry;
	}

	@Override
	public Object parse(Binding b) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, String> options = (Map<String, String>) b.getChildren()[1].getValue();
			String filePath = (String) b.getChildren()[2].getValue();

			int offset = 0;
			if (options.containsKey("offset"))
				offset = Integer.valueOf(options.get("offset"));

			int limit = 0;
			if (options.containsKey("limit"))
				limit = Integer.valueOf(options.get("limit"));

			FileInputStream is = new FileInputStream(new File(filePath));
			String parserName = options.get("parser");
			LogParser parser = null;
			if (parserName != null) {
				LogParserFactory factory = parserFactoryRegistry.get(parserName);
				if (factory == null)
					throw new IllegalStateException("log parser not found: " + parserName);

				parser = factory.createParser(convert(options));
			}

			return new TextFile(is, parser, offset, limit);
		} catch (Throwable t) {
			throw new RuntimeException("cannot create textfile source", t);
		}
	}

	private Properties convert(Map<String, String> options) {
		Properties p = new Properties();
		for (String key : options.keySet()) {
			String value = options.get(key);
			if (value != null)
				p.put(key, value);
		}

		return p;
	}

	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("textfile", this, k("textfile "), ref("option"), new StringPlaceholder());
		syntax.addRoot("textfile");
	}

}

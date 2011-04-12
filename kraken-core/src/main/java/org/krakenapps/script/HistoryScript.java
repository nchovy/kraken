package org.krakenapps.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptSession;
import org.krakenapps.api.ScriptUsage;

public class HistoryScript implements Script {
	private ScriptContext sc;

	@Override
	public void setScriptContext(ScriptContext context) {
		sc = context;
	}

	@ScriptUsage(description = "save history to file", arguments = {})
	public void save(String args[]) {
		if (args.length != 1) {
			sc.println("It needs file url (ex: file:///c:/temp/history.txt)");
			return;
		}
		String url = args[0];
		ScriptSession session = sc.getSession();
		
		PrintWriter writer = null;
		try {
			File file = new File(new URI(url));
			FileOutputStream fos = new FileOutputStream(file);
			writer = new PrintWriter(fos);
			
			Collection<String> commands = session.getCommandHistory();
			for (String command : commands) {
				writer.println(command.trim());
			}
		} catch (FileNotFoundException e) {
			sc.println(e.toString());
		} catch (URISyntaxException e) {
			sc.println(e.toString());
		} finally {
			if (writer != null)
				writer.close();
		}
	}

}

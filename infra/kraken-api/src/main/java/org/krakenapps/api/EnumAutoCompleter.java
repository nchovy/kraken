package org.krakenapps.api;

import java.util.ArrayList;
import java.util.List;

public abstract class EnumAutoCompleter implements ScriptAutoCompletionHelper {
	private String[] completions;

	public EnumAutoCompleter(String... completions) {
		this.completions = completions;
	}

	@Override
	public List<ScriptAutoCompletion> matches(ScriptSession session, String prefix) {
		List<ScriptAutoCompletion> l = new ArrayList<ScriptAutoCompletion>(completions.length);
		for (String completion : completions)
			if (completion.startsWith(prefix))
				l.add(new ScriptAutoCompletion(completion));

		return l;
	}
}

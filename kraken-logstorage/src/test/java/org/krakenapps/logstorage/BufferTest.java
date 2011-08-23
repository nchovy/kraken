package org.krakenapps.logstorage;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.krakenapps.logstorage.query.FileBufferList;
import org.krakenapps.logstorage.query.FileBufferMap;
import org.krakenapps.logstorage.query.command.Function;
import org.krakenapps.logstorage.query.command.FunctionCodec;

@Ignore
public class BufferTest {
	@Test
	public void list() throws IOException {
		FileBufferList<Map<String, String>> b = new FileBufferList<Map<String, String>>(new DefaultComparator());
		Map<String, String> m = new HashMap<String, String>();
		m.put("aa", "bb");
		b.add(m);
		for (Map<String, String> mm : b)
			System.out.println(mm);
		b.close();
	}

	@Test
	public void map() throws IOException {
		FileBufferMap<String, Function> m = new FileBufferMap<String, Function>(3, new FunctionCodec());
		m.put("test1", Function.getFunction("avg", "test"));
		m.put("test2", Function.getFunction("sum", "test"));
		m.put("test3", Function.getFunction("count", "test"));
		m.put("test4", Function.getFunction("dc", "test"));
		m.put("test5", Function.getFunction("avg", "test"));
		m.put("test6", Function.getFunction("avg", "test"));
		m.put("test7", Function.getFunction("avg", "test"));
		for(String key : m.keySet())
			System.out.printf("%s => %s\n", key, m.get(key));
		m.close();
	}

	private static class DefaultComparator implements Comparator<Map<String, String>> {
		@Override
		public int compare(Map<String, String> o1, Map<String, String> o2) {
			return o1.size() - o2.size();
		}
	}
}

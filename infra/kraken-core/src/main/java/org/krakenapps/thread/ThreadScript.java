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
package org.krakenapps.thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;

public class ThreadScript implements Script {
	private ScriptContext context;

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "stop thread", arguments = { @ScriptArgument(type = "int", name = "thread id", description = "target thread id") })
	public void stop(String[] args) {
		long threadId = -1;
		try {
			threadId = Long.parseLong(args[0]);
		} catch (NumberFormatException e) {
			context.println("thread id should be number");
			return;
		}

		Thread t = findThread(threadId);
		if (t != null)
			stopThread(t);
		else
			context.printf("thread [%s] not found\n", threadId);

	}

	@ScriptUsage(description = "interrupt thread", arguments = { @ScriptArgument(type = "int", name = "thread id", description = "target thread id") })
	public void interrupt(String[] args) {
		long threadId = -1;
		try {
			threadId = Long.parseLong(args[0]);
		} catch (NumberFormatException e) {
			context.println("thread id should be number");
			return;
		}

		Thread t = findThread(threadId);
		if (t != null)
			interruptThread(t);
		else
			context.printf("thread [%s] not found\n", threadId);
	}

	private Thread findThread(long threadId) {
		Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
		for (Thread t : stackTraces.keySet())
			if (t.getId() == threadId)
				return t;

		return null;
	}

	private void interruptThread(Thread t) {
		try {
			t.interrupt();
		} catch (SecurityException e) {
			context.println("security exception: " + e.getMessage());
		}
	}

	@SuppressWarnings("deprecation")
	private void stopThread(Thread t) {
		try {
			t.stop();
		} catch (SecurityException e) {
			context.println("security exception: " + e.getMessage());
		}
	}

	private static class ThreadOrder implements Comparator<Thread> {

		@Override
		public int compare(Thread o1, Thread o2) {
			return (int) (o1.getId() - o2.getId());
		}
	}

	public void list(String[] args) {
		Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
		List<Thread> threads = new ArrayList<Thread>(stackTraces.keySet());
		Collections.sort(threads, new ThreadOrder());

		for (Thread t : threads) {
			if (args.length > 0) {
				String substr = args[0].toLowerCase();
				if (!(t.getName().toLowerCase().contains(substr)
						|| t.getThreadGroup().getName().toLowerCase().contains(substr)
						|| t.getState().toString().toLowerCase().contains(substr) || Long.toString(t.getId()).contains(substr))) {
					continue;
				}
			}
			context.printf("[%3d] %s, Group: %s, State: %s, Priority: %d\n", t.getId(), t.getName(),
					t.getThreadGroup().getName(), t.getState(), t.getPriority());
		}
	}

	public void stacks(String[] args) {
		long tid = 0;
		String filter = null;
		if (args.length > 0) {
			filter = args[0];

			try {
				tid = Integer.parseInt(filter);
			} catch (NumberFormatException e) {
			}
		}

		Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
		List<Thread> threads = new ArrayList<Thread>(stackTraces.keySet());
		Collections.sort(threads, new ThreadOrder());

		for (Thread t : threads) {
			StackTraceElement[] stackTrace = stackTraces.get(t);

			if (tid == 0 && filter != null && !findClassAndMethod(stackTrace, filter))
				continue;
			else if (tid != t.getId())
				continue;

			context.printf("ID: %d, Name: %s, State: %s\n", t.getId(), t.getName(), t.getState().toString());

			for (StackTraceElement el : stackTrace) {
				context.printf("\t%s.%s %s\n", el.getClassName(), el.getMethodName(), getFileAndLineNumber(el));
			}

			context.println("");
		}
	}

	private boolean findClassAndMethod(StackTraceElement[] stackTrace, String target) {
		target = target.toLowerCase();
		for (StackTraceElement el : stackTrace) {
			String className = el.getClassName().toLowerCase();
			String methodName = el.getMethodName().toLowerCase();
			if (className.contains(target) || methodName.contains(target))
				return true;
		}

		return false;
	}

	private String getFileAndLineNumber(StackTraceElement el) {
		if (el.getFileName() != null && el.getLineNumber() > 0)
			return String.format("(%s:%d)", el.getFileName(), el.getLineNumber());
		else if (el.getFileName() != null && el.getLineNumber() <= 0)
			return String.format("(%s)", el.getFileName());
		else
			return "";
	}
}

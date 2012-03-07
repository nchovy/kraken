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
package org.krakenapps.console;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ConsoleHistoryManager implements TelnetArrowKeyHandler {
	private ConsoleController consoleController;
	private int index;

	/**
	 * working as history stack
	 */
	private LinkedList<String> lines;

	public ConsoleHistoryManager(ConsoleController consoleController) {
		this.consoleController = consoleController;
		this.lines = new LinkedList<String>();
		resetIndex();
	}

	enum Direction {
		Up, Down, Right, Left
	}

	@Override
	public boolean onPressUp() {
		boolean hasBeenEditing = index == -1;
		String line = previousLine();
		if (line == null)
			return true;

		if (hasBeenEditing) {
			assert (index == -1);
			String currentLine = consoleController.getLine();
			lines.push(currentLine);
			index = 1;
		}

		setLine(line);
		return true;
	}

	private void setLine(String line) {
		consoleController.setLine(line);
	}

	@Override
	public boolean onPressDown() {
		boolean hasBeenEditing = index == -1;
		if (hasBeenEditing)
			return true;

		String line = nextLine();
		if (line == null)
			return true;

		setLine(line);
		return true;
	}

	@Override
	public boolean onPressLeft() {
		return false;
	}

	@Override
	public boolean onPressRight() {
		return false;
	}

	@Override
	public boolean onOtherKeyPressed() {
		if (index == 0) {
			lines.removeFirst();
		}
		resetIndex();
		return true;
	}

	public void pushLine(String line) {
		// insert at the front
		if (lines.size() > 1 && lines.get(0).length() == 0)
			lines.removeFirst();
		if (lines.size() < 1 || !lines.get(0).equals(line))
			lines.addFirst(line);
		resetIndex();
	}

	private void resetIndex() {
		index = -1;
	}

	private String previousLine() {
		if (lines.size() == 0)
			return null;

		index++;
		if (index >= lines.size())
			index = lines.size() - 1;

		return stripCRLF(lines.get(index));
	}

	private String nextLine() {
		if (lines.size() == 0)
			return null;

		index--;
		if (index < 0)
			index = 0;

		return stripCRLF(lines.get(index));
	}

	private String stripCRLF(String line) {
		if (line.endsWith("\r\n"))
			return line.substring(0, line.length() - 2);
		else if (line.endsWith("\r") || line.endsWith("\n"))
			return line.substring(0, line.length() - 1);
		else
			return line;
	}

	public List<String> getCommandHistory() {
		List<String> commands = new LinkedList<String>(lines);
		Collections.reverse(commands);
		
		return commands;
	}
}

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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.krakenapps.ansicode.CursorPosCode;
import org.krakenapps.ansicode.EraseLineCode;
import org.krakenapps.ansicode.MoveCode;
import org.krakenapps.ansicode.MoveToCode;
import org.krakenapps.ansicode.EraseLineCode.Option;
import org.krakenapps.api.FunctionKeyEvent;
import org.krakenapps.api.ScriptAutoCompletion;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptOutputStream;
import org.krakenapps.api.FunctionKeyEvent.KeyCode;

public class ConsoleController {
	private ScriptContext sc;
	private LinkedList<String> dataList;
	private boolean hasLine;
	private TelnetArrowKeyHandler arrowKeyHandler;
	private ConsoleAutoComplete autoComplete;

	private int cursorPos;

	public ConsoleController(ScriptContext sc, ConsoleAutoComplete autoComplete) {
		this.sc = sc;
		dataList = new LinkedList<String>();
		setCursorPos(0);
		this.autoComplete = autoComplete;
	}

	public void addCharacter(String character) {
		ScriptOutputStream out = sc.getOutputStream();
		if (character.length() > 0 && (character.getBytes()[0] == (byte) 127 || character.getBytes()[0] == (byte) 8)) {
			eraseCharacter(character, false);
			try {
				throw new Exception("asdf");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (character.equals("\t")) {
			String input = peekLine();
			doAutoCompletion(input);
		} else if (character.equals("\n") || character.equals("\r")) {
			hasLine = true;

			if (sc.isEchoOn()) {
				dataList.addLast("\r");
				dataList.addLast("\n");

				out.print(new EraseLineCode(Option.CursorToEnd));
				Iterator<String> i = dataList.listIterator(cursorPos);
				while (i.hasNext())
					out.print(i.next());
			}
		} else {
			if (character.equals("\r"))
				return;
			dataList.add(cursorPos, character);
			increaseCursorPos();
			if (sc.isEchoOn()) {
				out.print(new EraseLineCode(Option.CursorToEnd));
				out.print(character);
				out.print(new CursorPosCode(CursorPosCode.Option.Save));
				Iterator<String> i = dataList.listIterator(cursorPos);
				while (i.hasNext())
					out.print(i.next());
				out.print(new CursorPosCode(CursorPosCode.Option.Restore));
			}
		}
	}

	private void doAutoCompletion(String input) {
		ScriptOutputStream out = sc.getOutputStream();
		String lastToken = getLastToken(input);

		List<ScriptAutoCompletion> terms = autoComplete.search(sc.getSession(), input);
		if (terms.size() > 1) {
			out.print("\r\n");
			for (ScriptAutoCompletion term : terms) {
				out.print(term.getSuggestion());
				out.print(" ");
			}
			out.print("\r\n");
			sc.printPrompt();
			String commonPrefix = extractCommonPrefix(terms);
			String remainingCommonPrefix = "";
			if (commonPrefix.length() >= lastToken.length())
				remainingCommonPrefix = commonPrefix.substring(lastToken.length());
			String semiCompletedLine = input + remainingCommonPrefix;
			if (semiCompletedLine.length() != 0) {
				out.print(semiCompletedLine);
			}
			for (int i = 0; i < remainingCommonPrefix.length(); ++i) {
				dataList.add(cursorPos, Character.toString(remainingCommonPrefix.charAt(i)));
				increaseCursorPos();
			}
		} else if (terms.size() == 1) {
			String term = terms.get(0).getCompletion();

			String completion = term.substring(lastToken.length());
			if (completion.length() > 0) {
				for (int i = 0; i < completion.length(); i++) {
					dataList.add(cursorPos, Character.toString(completion.charAt(i)));
					increaseCursorPos();
				}
				out.print(completion);
			}
		}
	}

	private String getLastToken(String input) {
		String[] tokens = ScriptArgumentParser.tokenize(input);
		if (tokens.length == 0)
			return "";

		String lastToken = "";
		if (!input.endsWith(" ")) {
			lastToken = tokens[tokens.length - 1];

			if (tokens.length == 1) {
				int p = lastToken.indexOf('.');
				if (p > 0)
					lastToken = lastToken.substring(p + 1);
			}
		}

		return lastToken;
	}

	private String extractCommonPrefix(List<ScriptAutoCompletion> terms) {
		if (terms.size() == 0)
			return new String("");
		else if (terms.size() == 1)
			return terms.get(0).getCompletion();
		else {
			String commonPrefix = terms.get(0).getCompletion();

			for (int i = 1; i < terms.size(); ++i) {
				String rhs = terms.get(i).getCompletion();
				for (int endPos = commonPrefix.length(); endPos >= 0; --endPos) {
					if (endPos == 0) {
						return new String("");
					}
					if (rhs.regionMatches(0, commonPrefix, 0, endPos)) {
						commonPrefix = commonPrefix.substring(0, endPos);
						break;
					}
				}
				if (commonPrefix.length() == 0)
					return commonPrefix;
			}
			return commonPrefix;
		}
	}

	public boolean onArrowKeyPressed(FunctionKeyEvent event) {
		ScriptOutputStream out = sc.getOutputStream();
		if (arrowKeyHandler == null)
			return true;

		if (event.isPressed(KeyCode.UP) || event.isPressed(KeyCode.CTRL_P)) {
			arrowKeyHandler.onPressUp();
			return true;
		} else if (event.isPressed(KeyCode.DOWN) || event.isPressed(KeyCode.CTRL_N)) {
			arrowKeyHandler.onPressDown();
			return true;
		} else if (event.isPressed(KeyCode.LEFT) || event.isPressed(KeyCode.CTRL_B)) {
			boolean handled = arrowKeyHandler.onPressLeft();
			if (!handled) {
				if (decreaseCursorPos())
					out.print(new MoveCode(MoveCode.Direction.Left, 1));
			}
			return true;
		} else if (event.isPressed(KeyCode.RIGHT) || event.isPressed(KeyCode.CTRL_F)) {
			boolean handled = arrowKeyHandler.onPressRight();
			if (!handled) {
				if (increaseCursorPos())
					out.print(new MoveCode(MoveCode.Direction.Right, 1));
			}
			return true;
		} else if (event.isPressed(KeyCode.HOME) || event.isPressed(KeyCode.CTRL_A)) {
			setCursorPos(0);
			out.print(new MoveToCode(sc.getSession().getPrompt().length() + 1));
			return true;
		} else if (event.isPressed(KeyCode.END) || event.isPressed(KeyCode.CTRL_E)) {
			setCursorPos(dataList.size());
			out.print(new MoveToCode(sc.getSession().getPrompt().length() + 1 + dataList.size()));
			return true;
		}

		return false;
	}

	public String getLine() {
		StringBuilder sb = new StringBuilder(1024);
		while (true) {
			if (dataList.isEmpty())
				break;

			String character = dataList.removeFirst();
			decreaseCursorPos();
			if (character == null)
				break;

			sb.append(character);

			if (character.equals("\n"))
				break;
		}

		hasLine = false;
		return sb.toString();
	}

	public boolean hasLine() {
		return hasLine;
	}

	public void setLine(String line) {
		ScriptOutputStream out = sc.getOutputStream();
		dataList = new LinkedList<String>();
		setCursorPos(0);
		for (int i = 0; i < line.length(); ++i) {
			dataList.add(cursorPos, new String(new char[] { line.charAt(i) }));
			increaseCursorPos();
		}

		revertLine();
		if (line.length() > 0)
			out.print(line);
	}

	public void eraseCharacter(String character, boolean isDelete) {
		ScriptOutputStream out = sc.getOutputStream();
		if (dataList.isEmpty())
			return;

		if (isDelete) {
			if (cursorPos == dataList.size())
				return;
			dataList.remove(cursorPos);
		} else {
			if (cursorPos == 0)
				return;
			dataList.remove(cursorPos - 1);
			decreaseCursorPos();

			if (sc.isEchoOn())
				out.print(new MoveCode(MoveCode.Direction.Left, 1));
		}

		out.print(new EraseLineCode(Option.CursorToEnd));
		out.print(new CursorPosCode(CursorPosCode.Option.Save));
		Iterator<String> i = dataList.listIterator(cursorPos);
		while (i.hasNext())
			out.print(i.next());
		out.print(new CursorPosCode(CursorPosCode.Option.Restore));
	}

	private void revertLine() {
		ScriptOutputStream out = sc.getOutputStream();
		out.print(new MoveToCode(0));
		out.print(new EraseLineCode(Option.EntireLine));
		out.print(sc.getSession().getPrompt());
	}

	private String peekLine() {
		StringBuilder sb = new StringBuilder(1024);
		Iterator<String> iter = dataList.iterator();
		while (iter.hasNext()) {
			sb.append(iter.next());
		}

		return sb.toString();
	}

	public void setArrowKeyHandler(TelnetArrowKeyHandler arrowKeyHandler) {
		this.arrowKeyHandler = arrowKeyHandler;
	}

	public TelnetArrowKeyHandler getArrowKeyHandler() {
		return this.arrowKeyHandler;
	}

	private void setCursorPos(int newPos) {
		cursorPos = newPos;
	}

	private boolean increaseCursorPos() {
		if (cursorPos + 1 <= dataList.size()) {
			cursorPos++;
			return true;
		} else
			return false;
	}

	private boolean decreaseCursorPos() {
		if (cursorPos - 1 >= 0) {
			cursorPos--;
			return true;
		} else
			return false;
	}

	public boolean onFunctionKeyPressed(FunctionKeyEvent ev) {
		switch (ev.getKeyCode()) {
		case CTRL_U:
			setLine("");
			return true;
		case DELETE:
			eraseCharacter("", true);
			return true;
		case BACKSPACE:
			eraseCharacter("", false);
			return true;
		}
		return onArrowKeyPressed(ev);
	}

	public void onCharacterInput(String message) {
		if (getArrowKeyHandler() != null) {
			getArrowKeyHandler().onOtherKeyPressed();
		}

		addCharacter((String) message);
	}
}

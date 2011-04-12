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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.krakenapps.ansicode.CursorPosCode;
import org.krakenapps.ansicode.EraseLineCode;
import org.krakenapps.ansicode.MoveCode;
import org.krakenapps.ansicode.EraseLineCode.Option;
import org.krakenapps.ansicode.MoveCode.Direction;
import org.krakenapps.api.FunctionKeyEvent;
import org.krakenapps.api.FunctionKeyEventListener;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptInputStream;
import org.krakenapps.api.ScriptOutputStream;
import org.krakenapps.api.FunctionKeyEvent.KeyCode;

public class ConsoleInputStream implements ScriptInputStream {
	private BlockingQueue<Character> buffer;
	private ScriptContext context;
	private Set<FunctionKeyEventListener> callbacks;

	public ConsoleInputStream(ScriptContext context) {
		this.buffer = new LinkedBlockingQueue<Character>();
		this.context = context;
		this.callbacks = new HashSet<FunctionKeyEventListener>();
	}

	@Override
	public void supplyInput(char character) {
		buffer.offer(character);
	}

	@Override
	public void supplyFunctionKey(FunctionKeyEvent keyEvent) {
		KeyCode c = keyEvent.getKeyCode();
		if (c == KeyCode.CTRL_C || c == KeyCode.CTRL_D)
			buffer.offer((char) 27);

		for (FunctionKeyEventListener callback : callbacks)
			callback.keyPressed(keyEvent);
	}

	@Override
	public char read() throws InterruptedException {
		Character character = buffer.take();
		if (character.charValue() == 27) {
			throw new InterruptedException();
		}

		if (context.isEchoOn()) {
			printEcho(character);
			if (character == '\r')
				printEcho('\n');
		}
		return character;
	}

	@Override
	public String readLine() throws InterruptedException {
		ReadLineHandler handler = new ReadLineHandler();
		try {
			addFunctionKeyEventListener(handler);
			return handler.getLine();
		} finally {
			removeFunctionKeyEventListener(handler);
		}
	}

	private boolean isBackspace(char character) {
		if (character == 127 || character == 8)
			return true;
		return false;
	}

	@Override
	public void flush() {
		buffer.clear();
	}

	@Override
	public void flush(Collection<Character> drain) {
		buffer.drainTo(drain);
	}

	private void printEcho(char c) {
		ScriptOutputStream outputStream = context.getOutputStream();
		if (isBackspace(c)) {
			outputStream.print(Character.toString('\b'));
		} else
			outputStream.print(Character.toString(c));
	}

	@Override
	public void addFunctionKeyEventListener(FunctionKeyEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("callback must be not null");

		callbacks.add(callback);
	}

	@Override
	public void removeFunctionKeyEventListener(FunctionKeyEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("callback must be not null");

		callbacks.remove(callback);
	}

	private class ReadLineHandler implements FunctionKeyEventListener {
		private StringBuilder builder;
		private int cursorIndex = 0;

		public String getLine() throws InterruptedException {
			ScriptOutputStream out = context.getOutputStream();
			builder = new StringBuilder(514);
			// noMoreBackspaceEcho = true;
			cursorIndex = 0;
			while (true) {
				char c = read();
				if (c != '\r' && c != '\n') {
					if (cursorIndex < builder.length()) {
						out.print(builder.toString().substring(cursorIndex));
						out.print(new MoveCode(Direction.Left, builder.length() - cursorIndex));
						builder.insert(cursorIndex, c);
					} else {
						builder.append(c);
					}

					cursorIndex++;
				}

				if (c == '\r' || c == '\n') {
					Character next = buffer.peek();
					if (next != null && c == '\r' && next == '\n')
						read(); // remove \n

					return builder.toString();
				}
			}
		}

		@Override
		public void keyPressed(FunctionKeyEvent e) {
			if (e.getKeyCode() == KeyCode.LEFT) {
				if (cursorIndex > 0) {
					cursorIndex--;
					context.getOutputStream().print(new MoveCode(MoveCode.Direction.Left, 1));
				}

			} else if (e.getKeyCode() == KeyCode.RIGHT) {
				if (builder != null && builder.length() > cursorIndex) {
					cursorIndex++;
					context.getOutputStream().print(new MoveCode(MoveCode.Direction.Right, 1));
				}
			} else if (e.getKeyCode() == KeyCode.BACKSPACE) {
				eraseCharacter(true);
			} else if (e.getKeyCode() == KeyCode.DELETE) {
				eraseCharacter(false);
			} else if (e.getKeyCode() == KeyCode.CTRL_C || e.getKeyCode() == KeyCode.CTRL_D) {
				buffer.offer((char) 27);
			}
		}

		private void eraseCharacter(boolean isBackspace) {
			if (isBackspace) {
				if (builder != null && cursorIndex > 0) {
					builder.deleteCharAt(cursorIndex - 1);
					cursorIndex--;

					ScriptOutputStream out = context.getOutputStream();
					if (context.isEchoOn()) {
						out.print(new MoveCode(MoveCode.Direction.Left, 1));
						out.print(new EraseLineCode(Option.CursorToEnd));
						String remain = builder.substring(cursorIndex);
						if (remain.length() != 0) {
							out.print(new CursorPosCode(CursorPosCode.Option.Save));
							out.print(builder.substring(cursorIndex));
							out.print(new CursorPosCode(CursorPosCode.Option.Restore));
						}
					}
				}
			} else {
				if (builder != null && cursorIndex < builder.length()) {
					builder.deleteCharAt(cursorIndex);
					ScriptOutputStream out = context.getOutputStream();

					if (context.isEchoOn()) {
						out.print(new EraseLineCode(Option.CursorToEnd));
						String remain = builder.substring(cursorIndex);
						if (remain.length() != 0) {
							out.print(new CursorPosCode(CursorPosCode.Option.Save));
							out.print(builder.substring(cursorIndex));
							out.print(new CursorPosCode(CursorPosCode.Option.Restore));
						}
					}
				}
			}
		}
	}

}

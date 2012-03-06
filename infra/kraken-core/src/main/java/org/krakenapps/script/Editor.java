/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.krakenapps.ansicode.ClearScreenCode;
import org.krakenapps.ansicode.EraseLineCode;
import org.krakenapps.ansicode.MoveToCode;
import org.krakenapps.ansicode.ScrollCode;
import org.krakenapps.ansicode.SetColorCode;
import org.krakenapps.ansicode.ClearScreenCode.Option;
import org.krakenapps.ansicode.SetColorCode.Color;
import org.krakenapps.api.FunctionKeyEvent;
import org.krakenapps.api.FunctionKeyEventListener;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.WindowSizeEventListener;
import org.krakenapps.api.FunctionKeyEvent.KeyCode;

public class Editor {
	private ScriptContext context;
	private boolean dirty;
	private int lineno;
	private int x = 0;
	private int y = 0;
	private List<String> lines;

	public Editor(ScriptContext context) {
		this.context = context;
	}

	private class EditorSizeChangedCallback implements WindowSizeEventListener {

		@Override
		public void sizeChanged(int width, int height) {
			try {
				render();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void open(final File f) throws IOException {
		lines = readFile(f);

		EditorSizeChangedCallback sizeCallback = new EditorSizeChangedCallback();
		FunctionKeyEventListener callback = new FunctionKeyEventListener() {
			@Override
			public void keyPressed(FunctionKeyEvent e) {
				try {
					KeyCode c = e.getKeyCode();
					// you should prevent additional rendering because it will
					// conflict with screen clear routine at exit.
					if (c == KeyCode.CTRL_C || c == KeyCode.CTRL_D)
						return;

					int maxHeight = getMaxHeight();

					// page down
					if (c == KeyCode.HOME || c == KeyCode.CTRL_A) {
						x = 0;
						renderStatus();
						moveTo();
					} else if (c == KeyCode.END || c == KeyCode.CTRL_E) {
						x = lines.get(lineno).length();
						renderStatus();
						moveTo();
					} else if (c == KeyCode.DOWN || c == KeyCode.CTRL_N) {
						if (lineno == lines.size() - 1)
							return;

						lineno++;
						if (y < maxHeight) {
							y++;
							renderStatus();
							moveTo();
						} else {
							// scroll down
							context.print(new SetColorCode(Color.Black, Color.White));
							context.print(new ScrollCode(true));
							context.print(new EraseLineCode(EraseLineCode.Option.EntireLine));
							context.print(new MoveToCode(0));
							context.print(lines.get(lineno));

							// title and cursor
							renderTitle();
							renderStatus();
							moveTo();
						}
					} else if (c == KeyCode.UP || c == KeyCode.CTRL_P) {
						if (lineno == 0)
							return;

						lineno--;
						if (y > 0) {
							y--;
							renderStatus();
							moveTo();
						} else {
							context.print(new SetColorCode(Color.Black, Color.White));
							context.print(new ScrollCode(false));

							renderTitle();
							renderStatus();

							context.print(new MoveToCode(1, 2));
							context.print(new EraseLineCode(EraseLineCode.Option.EntireLine));
							context.print(lines.get(lineno));
							// goto position after print
							moveTo();
						}
					} else if (c == KeyCode.RIGHT) {
						x++;
						renderStatus();
						moveTo();
					} else if (c == KeyCode.LEFT) {
						if (x > 0)
							x--;
						renderStatus();
						moveTo();
					} else if (c == KeyCode.BACKSPACE) {
						if (x > 0) {
							x--;

							String line = lines.get(lineno);
							String newLine = line.substring(0, x) + line.substring(x + 1);
							updateCurrentLine(lineno, newLine);
						}
					} else if (c == KeyCode.CTRL_S) {
						FileOutputStream fos = null;
						try {
							fos = new FileOutputStream(f);
							Charset utf8 = Charset.forName("utf-8");
							for (String line : lines) {
								fos.write(line.getBytes(utf8));
								fos.write("\n".getBytes());
							}

							dirty = false;
						} finally {
							if (fos != null)
								fos.close();

							renderStatus();
						}
					} else {
						render();
					}
				} catch (Exception ex) {
				}
			}
		};

		try {
			context.turnEchoOff();
			context.addWindowSizeEventListener(sizeCallback);
			context.getInputStream().addFunctionKeyEventListener(callback);

			render();

			// char input loop
			while (true) {
				char c = context.read();

				if (c == '\r' || c == '\n') {
					String line = getCurrentLine();
					String left = line.substring(0, x);
					String right = line.substring(x);

					// scroll down and redraw title and status
					context.print(new SetColorCode(Color.Black, Color.White));
					context.print(new ScrollCode(false));

					renderTitle();
					renderStatus();

					// cut right part off
					updateCurrentLine(lineno, left);

					if (y < getMaxHeight())
						y++;

					// set position and insert new line
					x = 0;
					lineno++;
					lines.add(lineno, right);

					// redraw from 0 to new line after scrolling
					moveTo(0, 0);
					for (int i = 0; i <= lineno; i++) {
						moveTo(0, i);
						context.print(new EraseLineCode(EraseLineCode.Option.EntireLine));
						context.print(lines.get(lineno - (lineno - i)));
					}

					// locate cursor
					moveTo();
				} else {
					String line = getCurrentLine();
					String newLine = line.substring(0, x) + c + line.substring(x);
					x++;

					updateCurrentLine(lineno, newLine);
				}
			}
		} catch (InterruptedException e) {

		} finally {
			context.removeWindowSizeEventListener(sizeCallback);
			context.getInputStream().removeFunctionKeyEventListener(callback);

			context.print(new SetColorCode(Color.Black, Color.White));
			context.print(new MoveToCode(1, 1));
			context.print(new ClearScreenCode(Option.EntireScreen));
			context.turnEchoOn();
		}
	}

	private int getMaxHeight() {
		int maxHeight = context.getHeight() - 3;
		return maxHeight;
	}

	private String getCurrentLine() {
		if (lines.size() > lineno)
			return lines.get(lineno);
		else {
			lines.add(lineno, "");
			return "";
		}
	}

	/**
	 * update line data, redraw, and locate cursor
	 */
	private void updateCurrentLine(int no, String newLine) {
		dirty = true;

		if (lines.size() > lineno)
			lines.remove(lineno);

		lines.add(lineno, newLine);
		context.print(new EraseLineCode(EraseLineCode.Option.EntireLine));
		context.print(new MoveToCode(0));
		context.print(newLine);
		renderStatus();
		moveTo();
	}

	private void render() throws FileNotFoundException, IOException {
		context.print(new MoveToCode(1, 1));
		context.print(new SetColorCode(Color.Black, Color.White, true));
		context.print(new ClearScreenCode(Option.EntireScreen));
		renderTitle();
		renderStatus();

		context.print(new SetColorCode(Color.Black, Color.White, false));
		context.print(new MoveToCode(1, 2));
		int height = context.getHeight() - 2;

		for (int i = 0; i < height; i++) {
			if (i >= lines.size())
				break;

			String line = lines.get(i);
			if (line.length() > context.getWidth())
				line = line.substring(context.getWidth());

			context.println(line);
		}

		moveTo();
	}

	private void renderTitle() {
		// title
		context.print(new MoveToCode(1, 1));
		context.print(new SetColorCode(Color.Blue, Color.White, false));
		context.print(new EraseLineCode(EraseLineCode.Option.EntireLine));
		context.print("Kraken Editor");
		context.print(new SetColorCode(Color.Black, Color.White, false));
	}

	private void renderStatus() {
		String dirtyStatus = dirty ? "[*]" : "[ ]";

		// status
		context.print(new MoveToCode(1, context.getHeight()));
		context.print(new SetColorCode(Color.Blue, Color.White, false));
		context.print(new EraseLineCode(EraseLineCode.Option.EntireLine));
		context.print(dirtyStatus + " Column: " + (x + 1) + ", Line: " + (lineno + 1));
		context.print(new SetColorCode(Color.Black, Color.White, false));
		moveTo();
	}

	private void moveTo(int x, int y) {
		context.print(new MoveToCode(x + 1, y + 2));
	}

	private void moveTo() {
		context.print(new MoveToCode(x + 1, y + 2));
	}

	private List<String> readFile(File f) {
		List<String> lines = new LinkedList<String>();
		FileInputStream is = null;
		try {
			is = new FileInputStream(f);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				lines.add(line);
			}
		} catch (IOException e) {
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}

		return lines;
	}

	@SuppressWarnings("unused")
	private void drawText(int x, int y, String s) {
		context.print(new MoveToCode(x, y));
		context.print(s);
	}

	@SuppressWarnings("unused")
	private void drawBox(int x1, int y1, int x2, int y2) {
		// first line
		context.print(new MoveToCode(x1, y1));
		context.print("+");
		for (int i = x1 + 1; i < x2; i++) {
			context.print("-");
		}
		context.print("+");

		// body
		for (int i = y1 + 1; i < y2; i++) {
			context.print(new MoveToCode(x1, i));
			context.print("|");
			for (int j = x1 + 1; j < x2; j++)
				context.print(" ");

			context.print("|");
		}

		// last line
		context.print(new MoveToCode(x1, y2));
		context.print("+");
		for (int i = x1 + 1; i < x2; i++) {
			context.print("-");
		}
		context.print("+");
	}

	@SuppressWarnings("unused")
	private void drawHorizontalLine(int x1, int x2, int y) {
		context.print(new MoveToCode(x1, y));
		context.print("+");
		for (int i = x1 + 1; i < x2; i++)
			context.print("-");
		context.print("+");
	}

	@SuppressWarnings("unused")
	private void drawVerticalLine(int x1, int y1, int y2) {
		context.print(new MoveToCode(x1, y1));
		for (int i = y1; i <= y2; i++) {
			context.print(new MoveToCode(x1, i));
			context.print("|");
		}

	}

}

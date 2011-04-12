package org.krakenapps.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.WindowSizeEventListener;
import org.krakenapps.api.FunctionKeyEvent.KeyCode;

public class EditorScript implements Script {
	private ScriptContext context;

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	private int lineno;
	private int x = 0;
	private int y = 0;
	private List<String> lines;

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

	public void open(String[] args) throws IOException {
		lines = readFile(args[0]);

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

					int maxHeight = context.getHeight() - 3;

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

			while (true) {
				context.read();
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
		context.print(new EraseLineCode(org.krakenapps.ansicode.EraseLineCode.Option.EntireLine));
		context.print("Kraken Editor");
		context.print(new SetColorCode(Color.Black, Color.White, false));
	}

	private void renderStatus() {
		// status
		context.print(new MoveToCode(1, context.getHeight()));
		context.print(new SetColorCode(Color.Blue, Color.White, false));
		context.print(new EraseLineCode(org.krakenapps.ansicode.EraseLineCode.Option.EntireLine));
		context.print("Column: " + (x + 1) + ", Line: " + (lineno + 1));
		context.print(new SetColorCode(Color.Black, Color.White, false));
	}

	private void moveTo() {
		context.print(new MoveToCode(x + 1, y + 2));
	}

	private List<String> readFile(String path) throws FileNotFoundException, IOException {
		File f = new File(path);
		FileInputStream is = new FileInputStream(f);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		List<String> lines = new ArrayList<String>();

		while (true) {
			String line = br.readLine();
			if (line == null)
				break;

			lines.add(line);
		}

		return lines;
	}

	private void drawText(int x, int y, String s) {
		context.print(new MoveToCode(x, y));
		context.print(s);
	}

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

	private void drawHorizontalLine(int x1, int x2, int y) {
		context.print(new MoveToCode(x1, y));
		context.print("+");
		for (int i = x1 + 1; i < x2; i++)
			context.print("-");
		context.print("+");
	}

	private void drawVerticalLine(int x1, int y1, int y2) {
		context.print(new MoveToCode(x1, y1));
		for (int i = y1; i <= y2; i++) {
			context.print(new MoveToCode(x1, i));
			context.print("|");
		}

	}

}

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
package org.krakenapps.ansicode;

public class SetColorCode extends AnsiEscapeCode {
	public enum Color {
		Black, Red, Green, Yellow, Blue, Magenta, Cyan, White, Reset;

		public static Color parse(int code) {
			for (Color color : Color.values()) {
				if (color.ordinal() == code)
					return color;
			}

			return null;
		}
	}

	private Color backColor;
	private Color foreColor;
	private boolean highIntensity;

	public SetColorCode(Color backgroundColor, Color foregroundColor) {
		this(backgroundColor, foregroundColor, false);
	}

	public SetColorCode(Color backgroundColor, Color foregroundColor, boolean highIntensity) {
		this.backColor = backgroundColor;
		this.foreColor = foregroundColor;
		this.highIntensity = highIntensity;
	}

	private int getColorCode(Color color) {
		switch (color) {
		case Black:
			return 0;
		case Red:
			return 1;
		case Green:
			return 2;
		case Yellow:
			return 3;
		case Blue:
			return 4;
		case Magenta:
			return 5;
		case Cyan:
			return 6;
		case White:
			return 7;
		case Reset:
			return 9;
		}
		throw new RuntimeException("Invalid color code. not reachable.");
	}

	private int getForeColor() {
		return 30 + getColorCode(foreColor);
	}

	private int getBackColor() {
		return 40 + getColorCode(backColor);
	}

	@Override
	public byte[] toByteArray() {
		String intensity = highIntensity ? "1" : "0";
		return wrapCSI(intensity + ";" + getForeColor() + ";" + getBackColor() + "m");
	}
}

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

public class MoveCode extends AnsiEscapeCode {
	public enum Direction {
		Up, Down, Left, Right
	}
	
	private Direction direction;
	private int cells;

	public MoveCode(Direction direction, int cells) {
		this.direction = direction;
		this.cells = cells;
	}

	private String getDirectionCode() {
		switch (direction) {
		case Up:
			return "A";
		case Down:
			return "B";
		case Right:
			return "C";
		case Left:
			return "D";
		}

		throw new RuntimeException("Invalid direction code. not reachable");
	}

	@Override
	public byte[] toByteArray() {
		return wrapCSI(cells + getDirectionCode());
	}
}

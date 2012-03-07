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

public class EraseLineCode extends AnsiEscapeCode {
	public enum Option {
		CursorToEnd, CursorToBeginning, EntireLine
	}

	private Option option;

	public EraseLineCode(Option option) {
		this.option = option;
	}

	@Override
	public byte[] toByteArray() {
		// NOTE: cursor position does not change.
		switch (option) {
		case CursorToBeginning:
			return wrapCSI("0K");
		case CursorToEnd:
			return wrapCSI("K");
		case EntireLine:
			return wrapCSI("2K");
		}

		throw new RuntimeException("Invalid EraseLine option, not reachable");
	}
}
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

public class ClearScreenCode extends AnsiEscapeCode {
	public enum Option {
		CursorToEnd, CursorToBeginning, EntireScreen
	}

	private Option option;

	public ClearScreenCode(Option option) {
		this.option = option;
	}

	@Override
	public byte[] toByteArray() {
		switch (option) {
		case CursorToBeginning:
			return wrapCSI("0J");
		case CursorToEnd:
			return wrapCSI("1J");
		case EntireScreen:
			return wrapCSI("2J");
		}
		throw new RuntimeException(
				"Invalid clear screen option. not reachable.");
	}
}

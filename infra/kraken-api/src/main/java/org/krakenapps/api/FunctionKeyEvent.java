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
package org.krakenapps.api;

public class FunctionKeyEvent {
	public enum KeyCode {
		UP,
		DOWN,
		LEFT,
		RIGHT,
		CTRL_A,
		CTRL_B,
		CTRL_C,
		CTRL_D, 
		CTRL_E,
		CTRL_F,
		CTRL_G,
		CTRL_L,
		CTRL_N,
		CTRL_O,
		CTRL_P,
		CTRL_Q,
		CTRL_R,
		CTRL_S,
		CTRL_T,
		CTRL_U, 
		CTRL_V,
		CTRL_W,
		CTRL_X,
		CTRL_Y,
		CTRL_Z,
		PAGE_UP,
		PAGE_DOWN,
		DELETE, 
		BACKSPACE, 
		HOME, 
		END,  
	}
	
	private KeyCode keyCode;
	
	public FunctionKeyEvent(KeyCode keyCode) {
		this.keyCode = keyCode;
	}
	
	public boolean isPressed(KeyCode keyCode) {
		return this.keyCode == keyCode; 
	}
	
	public KeyCode getKeyCode() {
		return keyCode;
	}
}

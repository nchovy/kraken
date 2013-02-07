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

public abstract class TelnetCommand {
	public static final byte SE = (byte) 240;
	public static final byte NOP = (byte) 241;
	public static final byte DataMark = (byte) 242;
	public static final byte Break = (byte) 243;
	public static final byte InterruptProcess = (byte) 244;
	public static final byte AbortOutput = (byte) 245;
	public static final byte AreYouThere = (byte) 246;
	public static final byte EraseCharacter = (byte) 247;
	public static final byte EraseLine = (byte) 248;
	public static final byte GoAhead = (byte) 249;
	public static final byte SB = (byte) 250;
	public static final byte Will = (byte) 251;
	public static final byte Wont = (byte) 252;
	public static final byte Do = (byte) 253;
	public static final byte Dont = (byte) 254;
	public static final byte InterpretAsControl = (byte) 255;

	public abstract byte[] toByteArray();
}

package org.krakenapps.termio;

public abstract class TerminalCommand {
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

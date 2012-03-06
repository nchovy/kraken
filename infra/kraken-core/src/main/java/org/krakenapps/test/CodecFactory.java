package org.krakenapps.test;

public class CodecFactory {
	private final Decoder decoder;
	private final Encoder encoder;
	
	public CodecFactory(){
		decoder = new Decoder();
		encoder = new Encoder();
	}
	
	public Decoder getDecoder(){
		return decoder;
	}
	public Encoder getEncoder(){
		return encoder;
	}
}

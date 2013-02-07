/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.pcap.decoder.netbios;

import java.nio.charset.Charset;

import org.krakenapps.pcap.util.Buffer;

public class NetBiosNameCodec {
	//00100000 -  Netbios name, length must be 32 (decimal)		||	0x20
    //11xxxxxx -  Label string pointer							||	0xc0
    //10xxxxxx -  Reserved										||	0x80
    //01xxxxxx -  Reserved										||	0x40
	//
	private NetBiosNameCodec() {
	}

	public static String readOemName(Buffer b , int length){
		byte[] buff = new byte[length];
		b.gets(buff);
		return new String(buff, Charset.forName("utf-8"));
	}
	public static String readOemName(Buffer b){
		int length = b.bytesBefore(new byte[]{0x00});
		byte[] buff = new byte[length];
		b.gets(buff);
		b.get();
		return new String(buff, Charset.forName("utf-8"));
	}
	public static byte[] encode(byte[] source) {
		byte[] b = new byte[source.length * 2];
		for (int i = 0; i < 16; i++) {
			b[i * 2] = (byte) (((source[i] & (0xf0)) >> 4) + 0x41);
			b[(i * 2) + 1] = (byte) ((source[i] & (0x0f)) + 0x41);
		}
		return b;
	}

	public static String readName(Buffer b) {
		byte tmp = b.get();
		if (tmp == (byte) 0x20) {
			int length = b.bytesBefore(new byte[] { 0x00 });
			byte[] buffer = new byte[length];
			b.gets(buffer);
			b.get();
			return decodeResourceName(buffer);
		} else {
			short signal = 0;
			signal = (short) (((tmp & 0xff) << 8) + b.get());
			if ((signal & 0xc000) == 0xc000) {
				b.mark();
				b.rewind();
				b.skip((signal & 0x3fff) + 1);
				int length = b.bytesBefore(new byte[] { 0x00 });
				byte[] buffer = new byte[length];
				b.gets(buffer);
				b.get();
				b.skip(6);
				return decodeResourceName(buffer);
			}
			else if((signal & 0x8000) == 0x8000){
				throw new IllegalStateException("Name Service Reserved Mask 0x80");
			}
			else if((signal & 0x4000) == 0x4000){
				throw new IllegalStateException("Name Service Reserved Mask 0x40");
			}
			else {
				
				throw new IllegalStateException("Name Service Read Name failed");
			}
		}
	}
	public static byte decodeDomainType(Buffer b){
		b.mark();
		int index = b.bytesBefore(new byte[]{0x00});
		b.skip(index -2);
		byte []domainBuff = new byte[2];
		b.gets(domainBuff);
		b.reset();
		int b0 = (domainBuff[0] - (byte) (0x41)) << 4;
		int b1 = (domainBuff[1] - (byte) (0x41));
		return (byte) (b0 + b1);	
	}
	public static String decodeResourceName(byte[] source) {
		byte[] b = new byte[source.length / 2];
		for (int i = 0; i < source.length / 2; i++) {
			int b0 = (source[i * 2] - (byte) (0x41)) << 4;
			int b1 = (source[(i * 2) + 1] - (byte) (0x41));
			b[i] = (byte) (b0 + b1);
		}

		// remove trailing null bytes
		byte[] buffer = null;
		int i = 0;
		for (i = 0; i < b.length; i++) {
			if ((b[i] == (byte) 0x00) || (b[i] == (byte) 0x20)) {
				break;
			}
		}
		buffer = new byte[i];
		for (int j = 0; j < i; j++) {
			buffer[j] = b[j];
		}

		return new String(buffer, Charset.forName("utf-8"));
	}

	public static String readSmbUnicodeName(Buffer b) {
		int length = b.bytesBefore(new byte[] { 0x00, 0x00 });
		// Name field in Transaction Resquest use {0x00,0x00) instead string
		byte[] buffer = new byte[length + 2];
		byte[] convert = new byte[(length + 2) / 2];
		b.gets(buffer);
		for (int i = 0; i < convert.length; i++) {
			convert[i] = buffer[i * 2];
		}
		if(length != 0){
			b.get();
		}
		else if(length ==0){
			return null;
		}
		return new String(convert, Charset.forName("utf-8"));
	}
	public static String readSmbUnicodeName(Buffer b , int length) {
		b.mark();
		byte tmp = b.get();
		byte tmp2 =  b.get();
		b.reset();
		if(tmp == 0x00 && tmp2 == 0x00 && length == 0){ // 0x00, 0x00 null string
			b.skip(2);
			return null;
		}
		else if(tmp == 0x00 && tmp2 == 0x00){ // skip 1byte padding
			// do nothing
		}
		else if(tmp == 0x00){
			b.skip(1);
		}
		// Name field in Transaction Request use {0x00,0x00) instead string
		if(length+2 == b.readableBytes()+1){
			length = length -1;
		}
		else if(length+2 == b.readableBytes()+2){
			length = length -2;
		}
		byte[] buffer = new byte[length];
		byte[] convert = new byte[(length) / 2];
		b.gets(buffer);
		for (int i = 0; i < convert.length; i++) {
			convert[i] = buffer[i * 2];
		}
		if( b.readableBytes() %2 == 1){
			b.get();
		}
		else if(b.readableBytes() >=2 ){
			b.get();
			b.get();
		}
		return new String(convert, Charset.forName("utf-8"));
	}
	public static String SmbNameConvertToString(byte[] b) {
		// Name field in Transaction Resquest use {0x00,0x00) instead string
		byte[] convert = new byte[(b.length + 1) / 2];
		for (int i = 0; i < convert.length; i++) {
			convert[i] = b[i * 2];
		}
		return new String(convert, Charset.forName("utf-8"));
	}
}

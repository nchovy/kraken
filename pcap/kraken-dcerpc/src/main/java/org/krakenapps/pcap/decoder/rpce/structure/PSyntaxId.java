/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class PSyntaxId {

	byte[] tmpbuffer;
	private String str1;
	private String str2;
	private String str3;
	private String str4;
	private String str5;
	private short if_ver;
	private short if_ver_minor;
	public void parse(Buffer b){
		tmpbuffer = new byte[16];
		b.gets(tmpbuffer);
		if_ver = ByteOrderConverter.swap(b.getShort());
		if_ver_minor = ByteOrderConverter.swap(b.getShort());
		//System.out.println("if_ver = "+ if_ver);
		//System.out.println("if_ver_minor =" + if_ver_minor);
	}
	public String getStr1() {
		return str1;
	}
	public void setStr1(String str1) {
		this.str1 = str1;
	}
	public String getStr2() {
		return str2;
	}
	public void setStr2(String str2) {
		this.str2 = str2;
	}
	public String getStr3() {
		return str3;
	}
	public void setStr3(String str3) {
		this.str3 = str3;
	}
	public String getStr4() {
		return str4;
	}
	public void setStr4(String str4) {
		this.str4 = str4;
	}
	public String getStr5() {
		return str5;
	}
	public void setStr5(String str5) {
		this.str5 = str5;
	}
	public short getIf_ver() {
		return if_ver;
	}
	public void setIf_ver(short if_ver) {
		this.if_ver = if_ver;
	}
	public short getIf_ver_minor() {
		return if_ver_minor;
	}
	public void setIf_ver_minor(short if_ver_minor) {
		this.if_ver_minor = if_ver_minor;
	}
	
}

/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.logparser.syslog.juniper.attack;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class LogVariableType {
	private static Map<String, Type> map = new HashMap<String, Type>();
	static {
		map.put("<dst-ip>", Type.DST_IP);
		map.put("<dst-port>", Type.DST_PORT);
		map.put("<interface-name>", Type.INTERFACE_NAME);
		map.put("<none>", Type.COUNT);
		map.put("<protocol>", Type.PROTOCOL);
		map.put("{ TCP | UDP | <protocol> }", Type.PROTOCOL);
		map.put("<src-ip>", Type.SRC_IP);
		map.put("<src-port>", Type.SRC_PORT);
		map.put("<zone-name>", Type.ZONE_NAME);
	}

	private Type type;
	
	public LogVariableType(Type type) {
		this.type = type;
	}

	public static LogVariableType from(String patternString) {
		Type type = map.get(patternString);
		return new LogVariableType(type);
	}

	@Override
	public String toString() {
		return type.name;
	}

	public Object parse(String s) {
		return type.parse(s);
	}
	
	private enum Type {
		DST_IP("dst-ip", Parser.IP), 
		DST_PORT("dst-port", Parser.INTEGER), 
		INTERFACE_NAME("interface-name", Parser.STRING), 
		COUNT("count", Parser.INTEGER), 
		PROTOCOL("protocol", Parser.STRING), 
		SRC_IP("src-ip", Parser.IP), 
		SRC_PORT("src-port", Parser.INTEGER), 
		ZONE_NAME("zone-name", Parser.STRING);
		
		private String name;
		private Parser parser;
		
		private Type(String name, Parser parser) {
			this.name = name; this.parser = parser;
		}
		
		public Object parse(String s) {
			return parser.parse(s);
		}
	}
	
	private enum Parser {
		STRING {
			@Override
			public Object parse(String s) {
				return s;
			}
		}, INTEGER {
			@Override
			public Object parse(String s) {
				return Integer.parseInt(s);
			}
		}, IP {
			@Override
			public Object parse(String s) {
				try {
					return InetAddress.getByName(s);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					return null;
				}
			}
		};
		
		public abstract Object parse(String s);
	}
}

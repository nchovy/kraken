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
package org.krakenapps.pcap.decoder.http;

/**
 * @author xeraph
 */
public class HttpHeaders {
	public static final String ACCEPT = "Accept";
	public static final String ACCEPT_ENCODING = "Accept-Encoding";
	public static final String ACCEPT_LANGUAGE = "Accept-Language";
	public static final String ALLOW = "Allow";
	public static final String AUTHORIZATION = "Authorization";
	public static final String BANDWIDTH = "Bandwidth";
	public static final String BLOCKSIZE = "Blocksize";
	public static final String CACHE_CONTROL = "Cache-Control";
	public static final String CONFERENCE = "Conference";
	public static final String CONNECTION = "Connection";
	public static final String CONTENT_BASE = "Content-Base";
	public static final String CONTENT_ENCODING = "Content-Encoding";
	public static final String CONTENT_LANGUAGE = "Content-Language";
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String CONTENT_LOCATION = "Content-Location";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CONTENT_RANGE = "Content-Range";
	public static final String DATE = "Date";
	public static final String EXPIRES = "Expires";
	public static final String FROM = "From";
	public static final String HOST = "Host";
	public static final String IF_MATCH = "If-Match";
	public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
	public static final String KEYMGMT = "KeyMgmt";
	public static final String LAST_MODIFIED = "Last-Modified";
	public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
	public static final String PROXY_REQUIRE = "Proxy-Require";
	public static final String PUBLIC = "Public";
	public static final String RANGE = "Range";
	public static final String REFERER = "Referer";
	public static final String REQUIRE = "Require";
	public static final String RETRY_AFTER = "Retry-After";
	public static final String RTP_INFO = "RTP-Info";
	public static final String SCALE = "Scale";
	public static final String SERVER = "Server";
	public static final String SESSION = "Session";
	public static final String SPEED = "Speed";
	public static final String TIMESTAMP = "Timestamp";
	public static final String UNSUPPORTED = "Unsupported";
	public static final String USER_AGENT = "User-Agent";
	public static final String VARY = "Vary";
	public static final String VIA = "Via";
	public static final String TRANSFER_ENCODING = "Transfer-Encoding";
	public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

	public static String canonicalize(String header) {
		if (header.equalsIgnoreCase(CONTENT_TYPE))
			return CONTENT_TYPE;
		else if (header.equalsIgnoreCase(CONTENT_LENGTH))
			return CONTENT_LENGTH;
		else if (header.equalsIgnoreCase(CONTENT_ENCODING)) 
			return CONTENT_ENCODING;
		else if (header.equalsIgnoreCase(CONTENT_RANGE))
			return CONTENT_RANGE;
		else if (header.equalsIgnoreCase(TRANSFER_ENCODING))
			return TRANSFER_ENCODING;
		return header;
	}

}

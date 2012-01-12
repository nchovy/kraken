/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.httpd;

import java.util.Map;
import java.util.HashMap;

public final class MimeTypes {
	private final static MimeTypes INSTANCE = new MimeTypes();

	private final Map<String, String> extMap;

	/**
	 * binary as a default
	 */
	private final String octetstream = "application/octet-stream";

	private MimeTypes() {
		this.extMap = new HashMap<String, String>();
		set("abs", "audio/x-mpeg");
		set("ai", "application/postscript");
		set("aif", "audio/x-aiff");
		set("aifc", "audio/x-aiff");
		set("aiff", "audio/x-aiff");
		set("aim", "application/x-aim");
		set("art", "image/x-jg");
		set("asf", "video/x-ms-asf");
		set("asx", "video/x-ms-asf");
		set("au", "audio/basic");
		set("avi", "video/x-msvideo");
		set("avx", "video/x-rad-screenplay");
		set("bcpio", "application/x-bcpio");
		set("bin", "application/octet-stream");
		set("bmp", "image/bmp");
		set("body", "text/html");
		set("cdf", "application/x-cdf");
		set("cer", "application/x-x509-ca-cert");
		set("class", "application/java");
		set("cpio", "application/x-cpio");
		set("csh", "application/x-csh");
		set("css", "text/css");
		set("dib", "image/bmp");
		set("doc", "application/msword");
		set("dtd", "application/xml-dtd");
		set("dv", "video/x-dv");
		set("dvi", "application/x-dvi");
		set("eps", "application/postscript");
		set("etx", "text/x-setext");
		set("exe", "application/octet-stream");
		set("gif", "image/gif");
		set("gk", "application/octet-stream");
		set("gtar", "application/x-gtar");
		set("gz", "application/x-gzip");
		set("hdf", "application/x-hdf");
		set("hqx", "application/mac-binhex40");
		set("htc", "text/x-component");
		set("htm", "text/html");
		set("html", "text/html");
		set("hqx", "application/mac-binhex40");
		set("ief", "image/ief");
		set("jad", "text/vnd.sun.j2me.app-descriptor");
		set("jar", "application/java-archive");
		set("java", "text/plain");
		set("jnlp", "application/x-java-jnlp-file");
		set("jpe", "image/jpeg");
		set("jpeg", "image/jpeg");
		set("jpg", "image/jpeg");
		set("js", "text/javascript");
		set("kar", "audio/x-midi");
		set("latex", "application/x-latex");
		set("m3u", "audio/x-mpegurl");
		set("mac", "image/x-macpaint");
		set("man", "application/x-troff-man");
		set("mathml", "application/mathml+xml");
		set("me", "application/x-troff-me");
		set("mid", "audio/x-midi");
		set("midi", "audio/x-midi");
		set("mif", "application/x-mif");
		set("mov", "video/quicktime");
		set("movie", "video/x-sgi-movie");
		set("mp1", "audio/x-mpeg");
		set("mp2", "audio/x-mpeg");
		set("mp3", "audio/x-mpeg");
		set("mpa", "audio/x-mpeg");
		set("mpe", "video/mpeg");
		set("mpeg", "video/mpeg");
		set("mpega", "audio/x-mpeg");
		set("mpg", "video/mpeg");
		set("mpv2", "video/mpeg2");
		set("ms", "application/x-wais-source");
		set("nc", "application/x-netcdf");
		set("oda", "application/oda");
		set("ogg", "application/ogg");
		set("pbm", "image/x-portable-bitmap");
		set("pct", "image/pict");
		set("pdf", "application/pdf");
		set("pgm", "image/x-portable-graymap");
		set("pic", "image/pict");
		set("pict", "image/pict");
		set("pls", "audio/x-scpls");
		set("png", "image/png");
		set("pnm", "image/x-portable-anymap");
		set("pnt", "image/x-macpaint");
		set("ppm", "image/x-portable-pixmap");
		set("ppt", "application/powerpoint");
		set("ps", "application/postscript");
		set("psd", "image/x-photoshop");
		set("qt", "video/quicktime");
		set("qti", "image/x-quicktime");
		set("qtif", "image/x-quicktime");
		set("ras", "image/x-cmu-raster");
		set("rdf", "application/rdf+xml");
		set("rgb", "image/x-rgb");
		set("rm", "application/vnd.rn-realmedia");
		set("roff", "application/x-troff");
		set("rtf", "application/rtf");
		set("rtx", "text/richtext");
		set("sh", "application/x-sh");
		set("shar", "application/x-shar");
		set("shtml", "text/x-server-parsed-html");
		set("sit", "application/x-stuffit");
		set("smf", "audio/x-midi");
		set("snd", "audio/basic");
		set("src", "application/x-wais-source");
		set("sv4cpio", "application/x-sv4cpio");
		set("sv4crc", "application/x-sv4crc");
		set("svg", "image/svg+xml");
		set("svgz", "image/svg+xml");
		set("swf", "application/x-shockwave-flash");
		set("t", "application/x-troff");
		set("tar", "application/x-tar");
		set("tcl", "application/x-tcl");
		set("tex", "application/x-tex");
		set("texi", "application/x-texinfo");
		set("texinfo", "application/x-texinfo");
		set("tif", "image/tiff");
		set("tiff", "image/tiff");
		set("tr", "application/x-troff");
		set("tsv", "text/tab-separated-values");
		set("txt", "text/plain");
		set("ulw", "audio/basic");
		set("ustar", "application/x-ustar");
		set("xbm", "image/x-xbitmap");
		set("xml", "text/xml");
		set("xpm", "image/x-xpixmap");
		set("xsl", "application/xml");
		set("xslt", "application/xslt+xml");
		set("xwd", "image/x-xwindowdump");
		set("vsd", "application/x-visio");
		set("vxml", "application/voicexml+xml");
		set("wav", "audio/x-wav");
		set("wbmp", "image/vnd.wap.wbmp");
		set("wml", "text/vnd.wap.wml");
		set("wmlc", "application/vnd.wap.wmlc");
		set("wmls", "text/vnd.wap.wmls");
		set("wmlscriptc", "application/vnd.wap.wmlscriptc");
		set("wrl", "x-world/x-vrml");
		set("xht", "application/xhtml+xml");
		set("xhtml", "application/xhtml+xml");
		set("xls", "application/vnd.ms-excel");
		set("xul", "application/vnd.mozilla.xul+xml");
		set("Z", "application/x-compress");
		set("z", "application/x-compress");
		set("zip", "application/zip");
		set("apk", "application/vnd.android.package-archive");
	}
	
	private void set(String ext, String mime) {
		this.extMap.put(ext, mime);
	}

	public String getByFile(String file) {
		if (file == null) {
			return octetstream;
		}

		int dot = file.lastIndexOf(".");
		if (dot < 0) {
			return octetstream;
		}

		String ext = file.substring(dot + 1);
		if (ext.length() < 1) {
			return octetstream;
		}

		return getByExtension(ext);
	}

	public String getByExtension(String ext) {
		if (ext == null) {
			return octetstream;
		}

		String mime = this.extMap.get(ext);
		if (mime == null)
			return octetstream;
		
		return mime;
	}

	public static MimeTypes instance() {
		return INSTANCE;
	}
}

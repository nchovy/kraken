/*
 * Copyright 2013 Future Systems, Inc.
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
package org.krakenapps.ca.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONConverter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.ca.CertificateAuthority;
import org.krakenapps.ca.CertificateMetadata;
import org.krakenapps.ca.CertificateMetadataIterator;
import org.krakenapps.ca.RevokedCertificate;
import org.krakenapps.ca.RevokedCertificateIterator;
import org.krakenapps.codec.Base64;

public class CertificateAuthorityFormatter {

	public static void convertToInternalFormat(InputStream is, OutputStream os) throws ParseException, IOException {
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(os, Charset.forName("utf-8"));
			JSONWriter jw = new JSONWriter(writer);
			jw.object();

			JSONTokener t = new JSONTokener(new InputStreamReader(is, Charset.forName("utf-8")));

			Map<String, Object> metadata = parseMetadata(t);
			jw.key("metadata").value(metadata);

			Integer version = (Integer) metadata.get("version");
			if (version != 1)
				throw new ParseException("unsupported authority data format version: " + version, -1);

			if (t.nextClean() != ',')
				return;

			Object data = t.nextValue();
			if (!data.equals("authority"))
				throw new ParseException("authority should be placed after metadata: token is " + data, -1);

			// "authority":{"key": "value"} or { "key" : []}
			t.nextClean(); // :
			t.nextClean(); // {

			if (t.nextClean() == '}')
				return;
			t.back();

			jw.key("collections");
			jw.object();

			metadata = new HashMap<String, Object>();
			int i = 0;
			while (true) {
				if (i++ != 0) {
					if (t.nextClean() == '}')
						break;
				}

				String key = (String) t.nextValue();

				t.nextClean(); // :

				if (key.equals("root_certificate")) {
					metadata.putAll(JSONConverter.parse((JSONObject) t.nextValue()));
					continue;
				}

				if (t.nextClean() == '[') {
					// collection name
					jw.key(key);

					// typed doc list
					jw.array();

					jw.value("list");

					// doc list begin
					jw.array();
					while (t.nextClean() != ']') {
						t.back();
						Map<String, Object> m = JSONConverter.parse((JSONObject) t.nextValue());
						jw.value(insertType(m));
						if (t.nextClean() != ',')
							t.back();
					}
					jw.endArray();

					jw.endArray();
					continue;
				} else
					t.back();

				metadata.put(key, t.nextValue());
			}
			jw.key("metadata");
			jw.array();
			jw.value("list");
			jw.array();

			Map<String, Object> m = new HashMap<String, Object>();
			m.put("type", "rootpw");
			m.put("password", metadata.get("key_password"));
			metadata.remove("key_password");
			jw.value(insertType(m));
			m = new HashMap<String, Object>();
			m.put("type", "crl");
			m.put("base_url", metadata.get("crl_base_url"));
			metadata.remove("crl_base_url");
			jw.value(insertType(m));
			if (metadata.containsKey("last_serial")) {
				m = new HashMap<String, Object>();
				m.put("type", "serial");
				m.put("serial", metadata.get("last_serial"));
				metadata.remove("last_serial");
				jw.value(insertType(m));
			}
			metadata.remove("name");
			jw.value(insertType(metadata));

			jw.endArray();
			jw.endArray();

			jw.endObject();
			jw.endObject();
			writer.flush();
		} catch (JSONException e) {
			throw new IOException(e);
		} finally {
		}

	}

	private static Object insertType(Map<String, Object> m) {
		for (String key : m.keySet()) {
			if (key.equals("binary"))
				m.put(key, createList("blob", m.get("binary")));
			else if (key.equals("not_before") || key.equals("issued_date") || key.equals("not_after") || key.equals("date"))
				m.put(key, createList("date", m.get(key)));
			else
				m.put(key, createList("string", m.get(key).toString()));
		}

		return createList("map", m);
	}

	private static List<Object> createList(String type, Object doc) {
		List<Object> l = new ArrayList<Object>(2);
		l.add(type);
		l.add(doc);

		return l;
	}

	private static Map<String, Object> parseMetadata(JSONTokener x) throws JSONException, IOException {
		if (x.nextClean() != '{') {
			throw x.syntaxError("A JSONObject text must begin with '{'");
		}

		Object key = x.nextValue();
		if (!key.equals("metadata"))
			throw x.syntaxError("confdb metadata should be placed first");

		x.nextClean();
		return JSONConverter.parse((JSONObject) x.nextValue());
	}

	@SuppressWarnings("unchecked")
	public static void exportAuthority(CertificateAuthority authority, OutputStream os) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		String binary = null;

		try {
			OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName("utf-8"));
			JSONWriter jw = new JSONWriter(writer);

			jw.object();

			jw.key("metadata");
			jw.object();
			jw.key("version").value(1);
			jw.key("date").value(sdf.format(new Date()));
			jw.endObject();

			jw.key("authority");
			jw.object();

			jw.key("certs");
			CertificateMetadataIterator certIt = null;
			try {
				certIt = authority.getCertificateIterator();
				jw.array();
				while (certIt.hasNext()) {
					CertificateMetadata cm = certIt.next();
					Map<String, Object> cert = (Map<String, Object>) PrimitiveConverter.serialize(cm);
					binary = new String(Base64.encode((byte[]) cert.get("binary")));
					cert.put("binary", binary);
					cert.put("not_before", sdf.format(cert.get("not_before")));
					cert.put("not_after", sdf.format(cert.get("not_after")));
					cert.put("issued_date", sdf.format(cert.get("issued_date")));
					jw.value(cert);
				}
				jw.endArray();
			} finally {
				if (certIt != null)
					certIt.close();
			}

			RevokedCertificateIterator revokeIt = null;
			try {
				revokeIt = authority.getRevokedCertificateIterator();
				jw.key("revoked");
				jw.array();
				while (revokeIt.hasNext()) {
					RevokedCertificate rc = revokeIt.next();
					Map<String, Object> revoked = (Map<String, Object>) PrimitiveConverter.serialize(rc);
					revoked.put("date", sdf.format(revoked.get("date")));
					jw.value(revoked);
				}
				jw.endArray();
			} finally {
				if (revokeIt != null)
					revokeIt.close();
			}

			jw.key("root_certificate");
			Map<String, Object> rootCertificate = (Map<String, Object>) PrimitiveConverter.serialize(authority
					.getRootCertificate());
			binary = new String(Base64.encode((byte[]) rootCertificate.get("binary")));
			rootCertificate.put("binary", binary);
			rootCertificate.put("not_before", sdf.format(rootCertificate.get("not_before")));
			rootCertificate.put("not_after", sdf.format(rootCertificate.get("not_after")));
			rootCertificate.put("issued_date", sdf.format(rootCertificate.get("issued_date")));
			rootCertificate.put("key_password", authority.getRootKeyPassword());
			jw.value(rootCertificate);

			jw.key("name").value(authority.getName());
			jw.key("crl_base_url").value(authority.getCrlDistPoint());
			jw.key("last_serial").value(authority.getLastSerial());

			jw.endObject();
			jw.endObject();
			writer.flush();
		} catch (JSONException e) {
			throw new IOException(e);
		}
	}
}

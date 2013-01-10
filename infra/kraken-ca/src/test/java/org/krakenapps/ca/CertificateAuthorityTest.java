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
package org.krakenapps.ca;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONConverter;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.krakenapps.ca.impl.CertificateAuthorityFormatter;
import org.krakenapps.codec.Base64;

public class CertificateAuthorityTest {

	@SuppressWarnings("unchecked")
	@Test
	public void exportAuthorityTest() throws MalformedURLException {
		String exportedString = exportAuthority();

		assertNotNull(exportedString);
		try {
			Map<String, Object> m = JSONConverter.parse(new JSONObject(exportedString));
			assertTrue(m.containsKey("metadata"));
			assertTrue(m.containsKey("authority"));
			Map<String, Object> authority = (Map<String, Object>) m.get("authority");
			assertTrue(authority.containsKey("certs"));
			assertTrue(authority.containsKey("root_certificate"));
			assertTrue(authority.containsKey("revoked"));
			assertTrue(authority.containsKey("name"));
			assertTrue(authority.containsKey("crl_base_url"));
			assertTrue(authority.containsKey("last_serial"));

			List<Map<String, Object>> certsMap = (List<Map<String, Object>>) authority.get("certs");
			for (Map<String, Object> cert : certsMap) {
				assertEquals(cert.get("type"), "JKS");
				assertArrayEquals("test_byte".getBytes(), Base64.decode((String) cert.get("binary")));
				assertEquals(cert.get("subject_dn").toString().substring(3), cert.get("serial"));
			}

			List<Map<String, Object>> revokeMap = (List<Map<String, Object>>) authority.get("revoked");
			for (Map<String, Object> revoke : revokeMap) {
				Integer serial = new Integer((String) revoke.get("serial"));
				assertEquals(RevocationReason.valueOf((String) revoke.get("reason")), RevocationReason.values()[serial]);
			}

			Map<String, Object> rootCertificate = (Map<String, Object>) authority.get("root_certificate");
			assertEquals(rootCertificate.get("subject_dn"), "CN=kraken");
			assertArrayEquals(Base64.decode((String) rootCertificate.get("binary")), "test_byte".getBytes());
			assertEquals(rootCertificate.get("key_password"), "kraken");
			assertEquals(rootCertificate.get("type"), "JKS");
			assertEquals(rootCertificate.get("serial"), "2");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void convertAuthorityFormatTest() throws MalformedURLException {
		String exportedString = exportAuthority();
		OutputStream os = null;
		InputStream is = null;
		String convertedString = null;
		try {
			os = new ByteArrayOutputStream();
			is = new ByteArrayInputStream(exportedString.getBytes());
			CertificateAuthorityFormatter.convertToInternalFormat(is, os);
			convertedString = os.toString();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}

		assertNotNull(convertedString);
		try {
			Map<String, Object> m = JSONConverter.parse(new JSONObject(convertedString));
			assertTrue(m.containsKey("metadata"));
			assertTrue(m.containsKey("collections"));

			Map<String, Object> collections = (Map<String, Object>) m.get("collections");
			assertTrue(collections.containsKey("certs"));
			assertTrue(collections.containsKey("revoked"));
			assertTrue(collections.containsKey("metadata"));

			List<Object> certs = (List<Object>) collections.get("certs");
			for (Object topList : certs) {
				if (topList.equals("list"))
					continue;
				List<Object> middleList = (List<Object>) topList;
				for (Object o : middleList) {
					List<Object> innerList = (List<Object>) o;
					for (Object certMap : innerList) {
						if (certMap.equals("map"))
							continue;

						Map<String, Object> cert = (Map<String, Object>) certMap;
						List<String> serial = (List<String>) cert.get("serial");
						String serialNumber = serial.get(0).equals("string") ? serial.get(1) : serial.get(0);

						List<String> subjectDn = (List<String>) cert.get("subject_dn");
						assertTrue(subjectDn.contains("string"));
						assertTrue(subjectDn.contains("CN=" + serialNumber));

						List<String> type = (List<String>) cert.get("type");
						assertTrue(type.contains("string"));
						assertTrue(type.contains("JKS"));

						List<String> binary = (List<String>) cert.get("binary");
						assertTrue(type.contains("string"));
						String encoded = binary.get(0).equals("blob") ? binary.get(1) : binary.get(0);
						assertArrayEquals("test_byte".getBytes(), Base64.decode(encoded));
					}
				}
			}

			List<Object> revoked = (List<Object>) collections.get("revoked");
			for (Object topList : revoked) {
				if (topList.equals("list"))
					continue;
				List<Object> middleList = (List<Object>) topList;
				for (Object o : middleList) {
					List<Object> innerList = (List<Object>) o;
					for (Object revokeMap : innerList) {
						if (revokeMap.equals("map"))
							continue;

						Map<String, Object> revokedCert = (Map<String, Object>) revokeMap;
						List<String> serial = (List<String>) revokedCert.get("serial");
						assertTrue(serial.contains("string"));
						String serialNumber = serial.get(0).equals("string") ? serial.get(1) : serial.get(0);

						List<String> reason = (List<String>) revokedCert.get("reason");
						assertEquals(RevocationReason.values()[new Integer(serialNumber)],
								RevocationReason.valueOf(reason.get(0).equals("string") ? reason.get(1) : reason.get(0)));
					}
				}
			}

			List<Object> metadata = (List<Object>) collections.get("metadata");
			for (Object topList : metadata) {
				if (topList.equals("list"))
					continue;

				List<Object> middleList = (List<Object>) topList;
				for (Object o : middleList) {

					List<Object> innerList = (List<Object>) o;
					for (Object metadataMap : innerList) {
						if (metadataMap.equals("map"))
							continue;
						Map<String, Object> doc = (Map<String, Object>) metadataMap;
						if (doc.containsKey("password")) {
							List<String> pw = (List<String>) doc.get("password");
							assertEquals("kraken", pw.get(0).equals("string") ? pw.get(1) : pw.get(0));
						} else if (doc.containsKey("base_url")) {
							List<String> crlBaseDispoint = (List<String>) doc.get("base_url");
							assertEquals("http://localhost", crlBaseDispoint.get(0).equals("string") ? crlBaseDispoint.get(1)
									: crlBaseDispoint.get(0));
						}
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private String exportAuthority() throws MalformedURLException {
		Collection<CertificateMetadata> certs = new ArrayList<CertificateMetadata>();
		List<RevokedCertificate> revoked = new ArrayList<RevokedCertificate>();
		for (int number = 0; number < 10; number++)
			certs.add(createCertificateMetadata(number, "CN=" + number));

		for (int number = 0; number < 10; number++)
			revoked.add(createRevokedCertificate(number, RevocationReason.values()[number]));

		MockCertificateAuthority mock = new MockCertificateAuthority("local", "2", "kraken", createCertificateMetadata(2,
				"CN=kraken"), new URL("http://localhost"), certs, revoked);

		String exportedString = null;
		OutputStream os = null;
		try {
			os = new ByteArrayOutputStream();
			CertificateAuthorityFormatter.exportAuthority(mock, os);
			exportedString = os.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
		}
		return exportedString;
	}

	private RevokedCertificate createRevokedCertificate(int serial, RevocationReason reason) {
		return new RevokedCertificate(String.valueOf(serial), new Date(), reason);
	}

	private CertificateMetadata createCertificateMetadata(int serial, String dn) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			CertificateMetadata root = new CertificateMetadata();
			root.setIssuedDate(sdf.parse("2013-01-01 10:10:10"));
			root.setNotAfter(sdf.parse("2014-01-01 10:10:10"));
			root.setNotBefore(sdf.parse("2012-01-01 10:10:10"));
			root.setSerial(String.valueOf(serial));
			root.setSubjectDn(dn);
			root.setType("JKS");
			root.setBinary("test_byte".getBytes());
			return root;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

}

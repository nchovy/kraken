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
package org.krakenapps.ca.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

public class CertificateExporter {
	private CertificateExporter() {
	}

	public static byte[] exportJks(X509Certificate cert, KeyPair keyPair, String keyPassword, X509Certificate caCert)
			throws Exception {
		KeyStore store = KeyStore.getInstance("JKS");
		store.load(null, null);
		store.setCertificateEntry("public", cert);
		store.setKeyEntry("private", keyPair.getPrivate(), keyPassword.toCharArray(), new Certificate[] { cert, caCert });

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		store.store(out, keyPassword.toCharArray());
		return out.toByteArray();
	}

	public static byte[] exportPkcs12(X509Certificate cert, KeyPair keyPair, String keyPassword, X509Certificate caCert)
			throws Exception {
		PKCS12BagAttributeCarrier bagAttr = (PKCS12BagAttributeCarrier) keyPair.getPrivate();
		bagAttr.setBagAttribute(new DERObjectIdentifier("1.2.840.113549.1.9.20"), new DERBMPString("public"));
		bagAttr.setBagAttribute(new DERObjectIdentifier("1.2.840.113549.1.9.21"), new SubjectKeyIdentifierStructure(
				keyPair.getPublic()));

		KeyStore pfx = KeyStore.getInstance("PKCS12", "BC");
		pfx.load(null, null);
		pfx.setCertificateEntry("public", cert);
		pfx.setKeyEntry("private", keyPair.getPrivate(), null, new Certificate[] { cert, caCert });

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pfx.store(out, keyPassword.toCharArray());
		return out.toByteArray();
	}

	public static void writePemFile(X509Certificate crt, RSAPrivateKey key, File output, boolean exportKey)
			throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException {
		OutputStream os = new FileOutputStream(output);
		PEMWriter writer = new PEMWriter(new PrintWriter(os), "BC");
		try {
			writer.writeObject(crt);
			if (exportKey)
				writer.writeObject(key);
		} finally {
			if (writer != null)
				writer.close();

			if (os != null)
				os.close();
		}
	}

	public static void writeCrtFile(Certificate crt, File output) throws KeyStoreException,
			CertificateEncodingException, IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(output);
			os.write(crt.getEncoded());
		} finally {
			if (os != null)
				os.close();
		}
	}
}

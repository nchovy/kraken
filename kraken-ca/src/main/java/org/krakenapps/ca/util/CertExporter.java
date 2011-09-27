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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

import org.bouncycastle.openssl.PEMWriter;

public class CertExporter {
	private CertExporter() {
	}
	
	public static void writePemFile(KeyStore store, String password, File output, boolean exportKey) throws KeyStoreException, IOException,
			UnrecoverableKeyException, NoSuchAlgorithmException {
		Certificate caCert = store.getCertificate("ca");
		OutputStream os = new FileOutputStream(output);
		PEMWriter writer = new PEMWriter(new PrintWriter(os), "PC");
		try {
			writer.writeObject(caCert);
			if (exportKey)
				writer.writeObject(store.getKey("ca-key", password.toCharArray()));
		} finally {
			if (writer != null)
				writer.close();

			if (os != null)
				os.close();
		}
	}

	public static void writeCrtFile(KeyStore store, File output) throws KeyStoreException, CertificateEncodingException,
			IOException {
		Certificate caCert = store.getCertificate("ca");
		OutputStream os = null;
		try {
			os = new FileOutputStream(output);
			os.write(caCert.getEncoded());
		} finally {
			if (os != null)
				os.close();
		}
	}
}

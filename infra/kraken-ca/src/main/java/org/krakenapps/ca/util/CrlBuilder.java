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

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import org.bouncycastle.x509.X509V2CRLGenerator;
import org.krakenapps.ca.RevokedCertificate;

@SuppressWarnings("deprecation")
public class CrlBuilder {
	private CrlBuilder() {
	}

	public static byte[] getCrl(X509Certificate caCert, PrivateKey caPrivateKey, List<RevokedCertificate> revokes) throws Exception {
		X509V2CRLGenerator generator = new X509V2CRLGenerator();
		generator.setIssuerDN(caCert.getIssuerX500Principal());

		generator.setThisUpdate(new Date());
		generator.setSignatureAlgorithm(caCert.getSigAlgName());

		for (RevokedCertificate r : revokes) {
			BigInteger serial = new BigInteger(r.getSerial());
			generator.addCRLEntry(serial, r.getRevocationDate(), r.getReason().ordinal());
		}

		X509CRL crl = generator.generate(caPrivateKey);
		return crl.getEncoded();
	}
}
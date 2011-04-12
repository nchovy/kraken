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
package org.krakenapps.ca;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public interface CertificateAuthority {
	X509Certificate createSelfSignedCertificate(KeyPair keyPair, String dn, Date notBefore, Date notAfter,
			String signatureAlgorithm) throws Exception;

	X509Certificate createCertificate(X509Certificate caCert, PrivateKey caKey, KeyPair keyPair, String dn,
			Map<String, String> attrs, Date notBefore, Date notAfter, String signatureAlgorithm) throws Exception;

	void exportPkcs12(String alias, File f, KeyPair keyPair, String keyPassword, Certificate cert, Certificate caCert)
			throws Exception;

	Collection<X509Certificate> getRootCertificates();

	Collection<String> getCertificates(String caCommonName);

	X509Certificate getCertificate(String caCommonName, String keyAlias, String keyPassword) throws Exception;

	byte[] getPfxFile(String caCommonName, String keyAlias, String keyPassword) throws IOException;

	X509Certificate issueSelfSignedCertificate(String dn, String signatureAlgorithm, int days, String password)
			throws Exception;

	X509Certificate issueCertificate(String caCommonName, String caPassword, String keyAlias, String keyPassword,
			String dn, String signatureAlgorithm, int days) throws Exception;
}

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

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.krakenapps.ca.CertificateRequest;

@SuppressWarnings("deprecation")
public class CertificateBuilder {
	private CertificateBuilder() {
	}

	public static X509Certificate createCertificate(CertificateRequest req) throws Exception {
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

		X509Principal subject = parseDn(req.getSubjectDn());
		X509Principal issuer = parseDn(req.getIssuerDn());

		certGen.setSerialNumber(req.getSerial());
		certGen.setIssuerDN(issuer);
		certGen.setSubjectDN(subject);
		certGen.setNotBefore(req.getNotBefore());
		certGen.setNotAfter(req.getNotAfter());
		certGen.setPublicKey(req.getKeyPair().getPublic());
		certGen.setSignatureAlgorithm(req.getSignatureAlgorithm());

		if (req.getCrlUrl() != null) {
			GeneralName gn = new GeneralName(6, new DERIA5String(req.getCrlUrl().toString()));

			ASN1EncodableVector vec = new ASN1EncodableVector();
			vec.add(gn);

			GeneralNames gns = new GeneralNames(new DERSequence(vec));
			DistributionPointName dpn = new DistributionPointName(0, gns);

			List<DistributionPoint> l = new ArrayList<DistributionPoint>();
			l.add(new DistributionPoint(dpn, null, null));

			CRLDistPoint crlDp = new CRLDistPoint(l.toArray(new DistributionPoint[0]));

			certGen.addExtension(new DERObjectIdentifier("2.5.29.31"), false, crlDp);
		}

		return certGen.generate(req.getIssuerKey(), "BC");
	}

	private static X509Principal parseDn(String dn) {
		Vector<Object> oids = new Vector<Object>();
		Vector<Object> values = new Vector<Object>();

		String[] tokens = dn.split(",");
		for (String token : tokens) {
			int p = token.indexOf('=');
			String key = token.substring(0, p).trim().toLowerCase();
			String value = token.substring(p + 1).trim();

			DERObjectIdentifier oid = (DERObjectIdentifier) X509Name.DefaultLookUp.get(key);
			if (oid != null) {
				oids.add(oid);
				values.add(value);
			}
		}

		return new X509Principal(oids, values);
	}

}

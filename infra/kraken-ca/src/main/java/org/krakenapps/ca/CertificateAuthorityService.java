/*
 * Copyright 2012 Future Systems, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Certificate authority service interface. You can manage multiple certificate
 * authorities.
 * 
 * @author xeraph
 */
public interface CertificateAuthorityService {
	/**
	 * @return all certificate authorities
	 */
	List<CertificateAuthority> getAuthorities();

	/**
	 * @param name
	 *            the certificate authority name
	 * @return the certificate authority by name
	 */
	CertificateAuthority getAuthority(String name);

	/**
	 * Create new certificate authority
	 * 
	 * @param name
	 *            the certificate authority name
	 * @param req
	 *            certificate sign request for self-signed root certificate
	 * @return the certificate authority
	 * @throws Exception
	 *             when root certificate generation failed
	 */
	CertificateAuthority createAuthority(String name, CertificateRequest req) throws Exception;

	CertificateAuthority importAuthority(String name, InputStream is) throws IOException;

	void exportAuthority(String name, OutputStream os) throws IOException;

	/**
	 * Remove certificate authority by name
	 * 
	 * @param name
	 *            the certificate authority's name
	 */
	void removeAuthority(String name);

	void addListener(CertificateAuthorityListener listener);

	void removeListener(CertificateAuthorityListener listener);
}

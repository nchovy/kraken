package org.krakenapps.ca;

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

	/**
	 * Remove certificate authority by name
	 * 
	 * @param name
	 *            the certificate authority's name
	 */
	void removeAuthority(String name);
}

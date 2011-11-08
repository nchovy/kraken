package org.krakenapps.ca;

import java.util.List;

public interface CertificateAuthorityService {
	List<CertificateAuthority> getAuthorities();

	CertificateAuthority getAuthority(String name);

	CertificateAuthority createAuthority(String name, CertificateRequest req) throws Exception;

	void removeAuthority(String name);
}

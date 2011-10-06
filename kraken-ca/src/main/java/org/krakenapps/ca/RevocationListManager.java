package org.krakenapps.ca;

import java.math.BigInteger;
import java.security.cert.X509CRLEntry;

public interface RevocationListManager {
	void revoke(BigInteger serialNumber);

	boolean isRevoked(BigInteger serialNumber);

	X509CRLEntry[] getRevocationList();
}

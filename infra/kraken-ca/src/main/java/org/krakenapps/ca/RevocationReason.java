package org.krakenapps.ca;

/**
 * Revocation reasons. Do NOT change item's order. Enum ordinal is used for rfc
 * compliant value.
 * 
 * @author xeraph
 */
public enum RevocationReason {
	Unspecified,
	KeyCompromise,
	CaCompromise,
	AffiliationChanged,
	Superseded,
	CessationOfOperation,
	CertificateHold,
	RemoveFromCrl,
	PrivilegeWithdrawn,
	AaCompromise
}

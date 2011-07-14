package org.krakenapps.crl;

public class AlgorithmClassifier {
	private AlgorithmClassifier() {
	}
	
	public static Algorithm getAlgorithm(String algorithmID) { 
		if(algorithmID.equals("1.2.840.113549.1.1.2")) {
			return Algorithm.MD2WithRSAEncryption;
		} else if(algorithmID.equals("1.2.840.113549.1.1.4")) {
			return Algorithm.MD5WithRSAEncryption;
		} else if(algorithmID.equals("1.2.840.113549.1.1.5")) {
			return Algorithm.SHA1WithRSAEncryption;
		} else if(algorithmID.equals("1.2.840.10040.4.3")) {
			return Algorithm.ID_DSA_With_SHA1;
		} else if(algorithmID.equals("1.2.840.113549.1.1")) { 
			return Algorithm.PKCS_1;
		} else if(algorithmID.equals("1.2.840.10046.2.1")) {
			return Algorithm.DHPublicNumber;
		} else if(algorithmID.equals("1.2.840.10040.4.1")) {
			return Algorithm.ID_DSA;
		}
		
		return null;
	}
}
package org.krakenapps.crl;

public class AlgorithmIdentifier {
	private String algorithmID;
	private Algorithm algorithm;
	
	public AlgorithmIdentifier(String algorithmID, Algorithm algorithm) {
		this.algorithmID = algorithmID;
		this.algorithm = algorithm;
	}
	
	public String getAlgorithmID() {
		return algorithmID;
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}
}
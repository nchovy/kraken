package org.krakenapps.rpc;

public enum RpcTrustLevel {
	Untrusted(1), Low(2), Medium(3), High(4);

	RpcTrustLevel(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static RpcTrustLevel parse(int code) {
		switch (code) {
		case 2:
			return Low;
		case 3:
			return Medium;
		case 4:
			return High;
		default:
			return Untrusted;
		}
	}

	private int code;
}

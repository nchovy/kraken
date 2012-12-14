package org.krakenapps.dns;

public enum DnsResponseCode {
	NO_ERROR(0, "No Error"), FORMAT_ERROR(1, "Format Error"), SERVER_FAILURE(2, "Server Failure"), NAME_ERROR(3, "Name Error"), NOT_IMPLEMENTED(
			4, "Not Implemented"), REFUSED(5, "Refused");

	DnsResponseCode(int code, String description) {
		this.code = code;
		this.description = description;
	}

	public static DnsResponseCode parse(int code) {
		for (DnsResponseCode c : values())
			if (c.getCode() == code)
				return c;

		return null;
	}

	public int getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	private int code;
	private String description;
}

package org.krakenapps.logdb;

public class LogQueryParseException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String type;
	private Integer offset;
	private String note;

	public LogQueryParseException(String type, int offset) {
		this(type, offset, null);
	}
	
	public LogQueryParseException(String type, int offset, String note) {
		this.type = type;
		this.offset = offset;
		this.note = note;
	}

	public String getType() {
		return type;
	}

	public Integer getOffset() {
		return offset;
	}

	@Override
	public String getMessage() {
		return "type=" + type + ", offset=" + offset + ", note=" + note;
	}

}

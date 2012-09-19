package org.krakenapps.msgbus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Token {

	private String tokenId = UUID.randomUUID().toString();
	private Date issuedDate = new Date();
	private Object data;

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public Date getIssuedDate() {
		return issuedDate;
	}

	public void setIssuedDate(Date issuedDate) {
		this.issuedDate = issuedDate;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return "token " + tokenId + ", issued=" + dateFormat.format(issuedDate) + ", data=" + data;
	}
}

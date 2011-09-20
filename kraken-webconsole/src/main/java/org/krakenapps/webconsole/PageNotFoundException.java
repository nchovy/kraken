package org.krakenapps.webconsole;

import java.io.IOException;

public class PageNotFoundException extends IOException {
	private static final long serialVersionUID = 1L;

	private String movedLocation;

	public PageNotFoundException() {
		this(null);
	}

	public PageNotFoundException(String movedLocation) {
		this.movedLocation = movedLocation;
	}

	public String getMovedLocation() {
		return movedLocation;
	}
}

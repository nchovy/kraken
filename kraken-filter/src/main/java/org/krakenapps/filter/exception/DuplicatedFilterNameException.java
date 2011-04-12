package org.krakenapps.filter.exception;

/**
 * Unchecked exception thrown when the requested filter id already exists at
 * filter loading.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class DuplicatedFilterNameException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String filterId;

	public DuplicatedFilterNameException(String filterId) {
		this.filterId = filterId;
	}

	public String getFilterId() {
		return filterId;
	}
}

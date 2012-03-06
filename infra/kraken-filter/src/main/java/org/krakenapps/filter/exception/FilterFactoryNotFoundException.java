package org.krakenapps.filter.exception;

/**
 * Unchecked exception thrown when the filter factory is not found.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class FilterFactoryNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String filterFactoryName;

	/**
	 * Creates an exception with filter factory name
	 * 
	 * @param filterFactoryName
	 *            filter class name in general, but can have alias name.
	 */
	public FilterFactoryNotFoundException(String filterFactoryName) {
		this.filterFactoryName = filterFactoryName;
	}

	/**
	 * Returns the filter factory name.
	 */
	public String getFilterFactoryName() {
		return filterFactoryName;
	}

}

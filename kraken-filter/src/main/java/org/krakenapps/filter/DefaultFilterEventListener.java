package org.krakenapps.filter;

/**
 * This class provides default implementations for the
 * {@link FilterEventListener} interface.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class DefaultFilterEventListener implements FilterEventListener {

	@Override
	public void onFilterBound(String fromFilterId, String toFilterId) {
	}

	@Override
	public void onFilterLoaded(String filterId) {
	}

	@Override
	public void onFilterUnbinding(String fromFilter, String toFilterId) {
	}

	@Override
	public void onFilterUnloading(String filterId) {
	}

	@Override
	public void onFilterSet(String filterId, String name, Object value) {
	}

	@Override
	public void onFilterUnset(String filterId, String name) {
	}

}

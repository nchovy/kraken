package org.krakenapps.grid.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.BundleManager;
import org.krakenapps.api.MavenResolveException;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcMethod;
import org.krakenapps.rpc.SimpleRpcService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

@Component(name = "grid-service-locator")
@Provides
public class GridNodeRpc extends SimpleRpcService {

	private BundleContext bc;

	@Requires
	private BundleManager bundleManager;

	@ServiceProperty(name = "rpc.name", value = "grid-node")
	private String name;

	public GridNodeRpc(BundleContext bc) {
		this.bc = bc;
	}

	@RpcMethod(name = "installBundle")
	public void installBundle(String groupId, String artifactId, String version) {
		try {
			// TODO: must check grid node master
			bundleManager.installBundle(null, groupId, artifactId, version);
		} catch (MavenResolveException e) {
			throw new RpcException(e.getMessage());
		}
	}

	@RpcMethod(name = "uninstallBundle")
	public void uninstallBundle(String bundleSymbolicName, String version) {
		// TODO: must check grid node master

		// find bundle
		Bundle bundle = null;

		for (Bundle b : bc.getBundles()) {
			if (b.getSymbolicName().equals(bundleSymbolicName) && b.getVersion().toString().equals(version)) {
				bundle = b;
				break;
			}
		}

		try {
			bundle.uninstall();
		} catch (BundleException e) {
			throw new RpcException(e.getMessage());
		}
	}
}

package org.krakenapps.geoip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;

public interface GeoIpService {
	GeoIpLocation locate(InetAddress address);

	void compileIpBlocks(File f) throws FileNotFoundException, IOException;
}

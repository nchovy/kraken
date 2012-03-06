package org.krakenapps.ftp;

import java.util.Collection;

public interface FtpProfileService {
	void createProfile(FtpConnectProfile profile);

	void removeProfile(String name);
	
	Collection<FtpConnectProfile> getProfiles();
	
	FtpConnectProfile getProfile(String name);
}

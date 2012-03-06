package org.krakenapps.msgbus;

public interface PackageMetadataProvider {
	String getKey();

	String getName();

	String getName(String locale);
}

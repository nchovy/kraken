package org.krakenapps.logstorage;

public interface LogStorageMonitor extends Runnable {
	int getMinFreeSpaceValue();

	DiskSpaceType getMinFreeSpaceType();

	DiskLackAction getDiskLackAction();

	void setMinFreeSpace(int value, DiskSpaceType type);

	void setDiskLackAction(DiskLackAction action);

	void registerDiskLackCallback(DiskLackCallback callback);

	void unregisterDiskLackCallback(DiskLackCallback callback);
}

package org.krakenapps.logstorage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogIndexingTask {
	private String tableName;
	private Date minDay;
	private Date maxDay;

	private FileSet diskFileSet;
	private FileSet onlineFileSet;
	private long diskMinId;
	private long diskMaxId;
	private long onlineMinId;
	private long onlineMaxId;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Date getMinDay() {
		return minDay;
	}

	public void setMinDay(Date minDay) {
		this.minDay = minDay;
	}

	public Date getMaxDay() {
		return maxDay;
	}

	public void setMaxDay(Date maxDay) {
		this.maxDay = maxDay;
	}

	public FileSet getOnlineFileSet() {
		return onlineFileSet;
	}

	public void setOnlineFileSet(FileSet onlineFileSet) {
		this.onlineFileSet = onlineFileSet;
	}

	public long getDiskMinId() {
		return diskMinId;
	}

	public void setDiskMinId(long diskMinId) {
		this.diskMinId = diskMinId;
	}

	public long getDiskMaxId() {
		return diskMaxId;
	}

	public void setDiskMaxId(long diskMaxId) {
		this.diskMaxId = diskMaxId;
	}

	public long getOnlineMinId() {
		return onlineMinId;
	}

	public void setOnlineMinId(long onlineMinId) {
		this.onlineMinId = onlineMinId;
	}

	public long getOnlineMaxId() {
		return onlineMaxId;
	}

	public void setOnlineMaxId(long onlineMaxId) {
		this.onlineMaxId = onlineMaxId;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String min = "unbound";
		String max = "unbound";

		if (minDay != null)
			min = dateFormat.format(minDay);
		if (maxDay != null)
			max = dateFormat.format(maxDay);

		return String.format("indexing task for table %s, duration [%s~%s]", tableName, min, max);
	}

}

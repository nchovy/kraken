package org.krakenapps.logdb.mapreduce;

import java.util.Date;

public class RemoteQuery {
	private String nodeGuid;
	private int id;
	private String queryString;
	private boolean running;
	private boolean end;
	private Date createDate;
	private Date startDate;
	private Date endDate;

	public RemoteQuery(String nodeGuid, int id, String queryString) {
		this.nodeGuid = nodeGuid;
		this.id = id;
		this.queryString = queryString;
		this.createDate = new Date();
	}

	public String getNodeGuid() {
		return nodeGuid;
	}

	public int getId() {
		return id;
	}

	public String getQueryString() {
		return queryString;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
		if (running)
			this.startDate = new Date();
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
		if (end)
			this.endDate = new Date();
	}

	public Date getCreateDate() {
		return createDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	@Override
	public String toString() {
		return "node=" + nodeGuid + ", id=" + id + ", query=" + queryString + ", running=" + running + ", end=" + end
				+ ", created=" + createDate + ", started=" + startDate + ", ended=" + endDate + "]";
	}

}

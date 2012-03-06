/*
 * Copyright 2009 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.dns;

/**
 * @author mindori
 */
public class DnsAuthoritativeNameServer {
	private short queryName;
	private short queryType;
	private short queryClass;
	private int timeToLive;
	private short dataLength;
	private String nameServer;
	private String primaryNameServer;
	private String responsibleAuthority_Mailbox;
	private int serialNumber;
	private int refreshInterval;
	private int retryInterval;
	private int expirationLimit;
	private int minimumTimeToLive;

	public short getQueryName() {
		return queryName;
	}

	public void setQueryName(short queryName) {
		this.queryName = queryName;
	}

	public short getQueryType() {
		return queryType;
	}

	public void setQueryType(short queryType) {
		this.queryType = queryType;
	}

	public short getQueryClass() {
		return queryClass;
	}

	public void setQueryClass(short queryClass) {
		this.queryClass = queryClass;
	}

	public int getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	public short getDataLength() {
		return dataLength;
	}

	public void setDataLength(short dataLength) {
		this.dataLength = dataLength;
	}

	public String getNameServer() {
		return nameServer;
	}

	public void setNameServer(String nameServer) {
		this.nameServer = nameServer;
	}

	public String getPrimaryNameServer() {
		return primaryNameServer;
	}

	public void setPrimaryNameServer(String primaryNameServer) {
		this.primaryNameServer = primaryNameServer;
	}

	public String getResponsibleAuthority_Mailbox() {
		return responsibleAuthority_Mailbox;
	}

	public void setResponsibleAuthority_Mailbox(
			String responsibleAuthority_Mailbox) {
		this.responsibleAuthority_Mailbox = responsibleAuthority_Mailbox;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}

	public int getRefreshInterval() {
		return refreshInterval;
	}

	public void setRefreshInterval(int refreshInterval) {
		this.refreshInterval = refreshInterval;
	}

	public int getRetryInterval() {
		return retryInterval;
	}

	public void setRetryInterval(int retryInterval) {
		this.retryInterval = retryInterval;
	}

	public int getExpirationLimit() {
		return expirationLimit;
	}

	public void setExpirationLimit(int expirationLimit) {
		this.expirationLimit = expirationLimit;
	}

	public int getMinimumTimeToLive() {
		return minimumTimeToLive;
	}

	public void setMinimumTimeToLive(int minimumTimeToLive) {
		this.minimumTimeToLive = minimumTimeToLive;
	}
}

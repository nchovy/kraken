/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.pcap.decoder.netbios.rr;

public class Statistics {
	private byte[] unitID;
	private byte jumpers;
	private byte testResult;
	private short versionNum;
	private short periodOfStatistics;
	private short numberOfCRCs;
	private short numberAlignmentErrors;
	private short numberOfCollisions;
	private short numberSendAborts;
	private int numberGoodSends;
	private int numberGoodReceives;
	private short numberRetransmits;
	private short numberNoResourceConditions;
	private short numberFreeCommandBlocks;
	private short totalNumberCommandBlocks;
	private short maxTotalNumberCommandBlocks;
	private short numberPendingSessions;
	private short maxNumberPendingSessions;
	private short maxTotalSessionPossible;
	private short sessionDataPacketSize;

	public void allocUnitID(int count) {
		unitID = new byte[count];
	}

	public byte[] getUnitID() {
		return unitID;
	}

	public void setUnitID(byte[] unitID) {
		this.unitID = unitID;
	}

	public byte getJumpers() {
		return jumpers;
	}

	public void setJumpers(byte jumpers) {
		this.jumpers = jumpers;
	}

	public byte getTestResult() {
		return testResult;
	}

	public void setTestResult(byte testResult) {
		this.testResult = testResult;
	}

	public short getVersionNum() {
		return versionNum;
	}

	public void setVersionNum(short versionNum) {
		this.versionNum = versionNum;
	}

	public short getPeriodOfStatistics() {
		return periodOfStatistics;
	}

	public void setPeriodOfStatistics(short periodOfStatistics) {
		this.periodOfStatistics = periodOfStatistics;
	}

	public short getNumberOfCRCs() {
		return numberOfCRCs;
	}

	public void setNumberOfCRCs(short numberOfCRCs) {
		this.numberOfCRCs = numberOfCRCs;
	}

	public short getNumberAlignmentErrors() {
		return numberAlignmentErrors;
	}

	public void setNumberAlignmentErrors(short numberAlignmentErrors) {
		this.numberAlignmentErrors = numberAlignmentErrors;
	}

	public short getNumberOfCollisions() {
		return numberOfCollisions;
	}

	public void setNumberOfCollisions(short numberOfCollisions) {
		this.numberOfCollisions = numberOfCollisions;
	}

	public short getNumberSendAborts() {
		return numberSendAborts;
	}

	public void setNumberSendAborts(short numberSendAborts) {
		this.numberSendAborts = numberSendAborts;
	}

	public int getNumberGoodSends() {
		return numberGoodSends;
	}

	public void setNumberGoodSends(int numberGoodSends) {
		this.numberGoodSends = numberGoodSends;
	}

	public int getNumberGoodReceives() {
		return numberGoodReceives;
	}

	public void setNumberGoodReceives(int numberGoodReceives) {
		this.numberGoodReceives = numberGoodReceives;
	}

	public short getNumberRetransmits() {
		return numberRetransmits;
	}

	public void setNumberRetransmits(short numberRetransmits) {
		this.numberRetransmits = numberRetransmits;
	}

	public short getNumberNoResourceConditions() {
		return numberNoResourceConditions;
	}

	public void setNumberNoResourceConditions(short numberNoResourceConditions) {
		this.numberNoResourceConditions = numberNoResourceConditions;
	}

	public short getNumberFreeCommandBlocks() {
		return numberFreeCommandBlocks;
	}

	public void setNumberFreeCommandBlocks(short numberFreeCommandBlocks) {
		this.numberFreeCommandBlocks = numberFreeCommandBlocks;
	}

	public short getTotalNumberCommandBlocks() {
		return totalNumberCommandBlocks;
	}

	public void setTotalNumberCommandBlocks(short totalNumberCommandBlocks) {
		this.totalNumberCommandBlocks = totalNumberCommandBlocks;
	}

	public short getMaxTotalNumberCommandBlocks() {
		return maxTotalNumberCommandBlocks;
	}

	public void setMaxTotalNumberCommandBlocks(short maxTotalNumberCommandBlocks) {
		this.maxTotalNumberCommandBlocks = maxTotalNumberCommandBlocks;
	}

	public short getNumberPendingSessions() {
		return numberPendingSessions;
	}

	public void setNumberPendingSessions(short numberPendingSessions) {
		this.numberPendingSessions = numberPendingSessions;
	}

	public short getMaxNumberPendingSessions() {
		return maxNumberPendingSessions;
	}

	public void setMaxNumberPendingSessions(short maxNumberPendingSessions) {
		this.maxNumberPendingSessions = maxNumberPendingSessions;
	}

	public short getMaxTotalSessionPossible() {
		return maxTotalSessionPossible;
	}

	public void setMaxTotalSessionPossible(short maxTotalSessionPossible) {
		this.maxTotalSessionPossible = maxTotalSessionPossible;
	}

	public short getSessionDataPacketSize() {
		return sessionDataPacketSize;
	}

	public void setSessionDataPacketSize(short sessionDataPacketSize) {
		this.sessionDataPacketSize = sessionDataPacketSize;
	}

	@Override
	public String toString() {
		return String
				.format("Statistics\n"
						+ "unitId = %s , jumpers = 0x%s , testResult = 0x%s , versionNum = 0x%s\n"
						+ "periodOfStatistics = 0x%s , numberOfCRCs = 0x%s , numberAllignmentErrors = 0x%s\n"
						+ "numberOfCollisions = 0x%s , numberSendAborts = 0x%s , numberGoodSends = 0x%s\n"
						+ "numbergoodReceives = 0x%s , numberRetrabsnyts = 0x%s , nymberNoResourceConditions = 0x%s\n"
						+ "numberFreeCommandBlocks = 0x%s , totalNumberCommandBlocks = 0x%s , maxTotalNumberCommandBlocks = 0x%s\n"
						+ "numberPendingSessions = 0x%s , maxNumberPendingSessions = 0x%s , maxtotalSessionPossible = 0x%s\n"
						+ "sessionDataPacketSize = 0x%s\n ", this.unitID.toString(), Integer.toHexString(this.jumpers),
						Integer.toHexString(this.testResult), Integer.toHexString(this.versionNum),
						Integer.toHexString(this.periodOfStatistics), Integer.toHexString(this.numberOfCRCs),
						Integer.toHexString(this.numberAlignmentErrors), Integer.toHexString(this.numberGoodReceives),
						Integer.toHexString(this.numberRetransmits), Integer.toHexString(this.numberGoodSends),
						Integer.toHexString(this.numberFreeCommandBlocks),
						Integer.toHexString(this.totalNumberCommandBlocks),
						Integer.toHexString(this.maxTotalNumberCommandBlocks),
						Integer.toHexString(this.numberPendingSessions),
						Integer.toHexString(this.maxNumberPendingSessions),
						Integer.toHexString(this.maxTotalSessionPossible),
						Integer.toHexString(this.sessionDataPacketSize));
	}
}

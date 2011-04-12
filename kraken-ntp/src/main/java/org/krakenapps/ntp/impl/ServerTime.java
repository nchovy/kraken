/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.ntp.impl;

import java.util.Date;

/**
 * @author delmitz
 */
public class ServerTime {
	private int stratum;
	private byte poll;
	private byte precision;
	private byte[] rootDelay;
	private byte[] rootDispersion;
	private byte[] referenceIdentifier;
	private long reference;
	private long originate;
	private long receive;
	private long transmit;
	private long destination;

	public int getStratum() {
		return stratum;
	}

	public void setStratum(int stratum) {
		this.stratum = stratum;
	}

	public byte getPoll() {
		return poll;
	}

	public void setPoll(byte poll) {
		this.poll = poll;
	}

	public byte getPrecision() {
		return precision;
	}

	public void setPrecision(byte precision) {
		this.precision = precision;
	}

	public byte[] getRootDelay() {
		return rootDelay;
	}

	public void setRootDelay(byte[] rootDelay) {
		this.rootDelay = rootDelay;
	}

	public byte[] getRootDispersion() {
		return rootDispersion;
	}

	public void setRootDispersion(byte[] rootDispersion) {
		this.rootDispersion = rootDispersion;
	}

	public byte[] getReferenceIdentifier() {
		return referenceIdentifier;
	}

	public void setReferenceIdentifier(byte[] referenceIdentifier) {
		this.referenceIdentifier = referenceIdentifier;
	}

	public Date getReference() {
		return new Date(reference);
	}

	public void setReference(long reference) {
		this.reference = reference;
	}

	public Date getOriginate() {
		return new Date(originate);
	}

	public void setOriginate(long originate) {
		this.originate = originate;
	}

	public Date getReceive() {
		return new Date(receive);
	}

	public void setReceive(long receive) {
		this.receive = receive;
	}

	public Date getTransmit() {
		return new Date(transmit);
	}

	public void setTransmit(long transmit) {
		this.transmit = transmit;
	}

	public Date getDestination() {
		return new Date(destination);
	}

	public void setDestination(long destination) {
		this.destination = destination;
	}

	public long getRoundtripDelay() {
		long roundtrip = (destination - originate) - (transmit - receive);
		return roundtrip;
	}

	public long getClockOffset() {
		long offset = ((receive - originate) + (transmit - destination)) / 2;
		return offset;
	}
}

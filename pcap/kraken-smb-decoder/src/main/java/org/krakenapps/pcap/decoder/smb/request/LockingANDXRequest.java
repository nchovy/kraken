package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.LockingANDXRange32;
import org.krakenapps.pcap.decoder.smb.structure.LockingANDXRange64;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x24
public class LockingANDXRequest implements SmbData{

	boolean malformed = false;
	byte wordCount;
	byte andxCommand;
	byte andxReserved;
	short andxOffset;
	short fid;
	byte typeOfLock;
	byte newOpLockLevel;
	int timeout;
	short numberOfreqedUnlocks;
	short numberOfreqedLocks;
	//data
	short byteCount;
	LockingANDXRange32 []unlocks32;// 10 byte
	LockingANDXRange64 []unlocks64;// 20 byte
	LockingANDXRange32 []Locks32;
	LockingANDXRange64 []locks64;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public byte getAndxCommand() {
		return andxCommand;
	}
	public void setAndxCommand(byte andxCommand) {
		this.andxCommand = andxCommand;
	}
	public byte getAndxReserved() {
		return andxReserved;
	}
	public void setAndxReserved(byte andxReserved) {
		this.andxReserved = andxReserved;
	}
	public short getAndxOffset() {
		return andxOffset;
	}
	public void setAndxOffset(short andxOffset) {
		this.andxOffset = andxOffset;
	}
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public byte getTypeOfLock() {
		return typeOfLock;
	}
	public void setTypeOfLock(byte typeOfLock) {
		this.typeOfLock = typeOfLock;
	}
	public byte getNewOpLockLevel() {
		return newOpLockLevel;
	}
	public void setNewOpLockLevel(byte newOpLockLevel) {
		this.newOpLockLevel = newOpLockLevel;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public short getNumberOfreqedUnlocks() {
		return numberOfreqedUnlocks;
	}
	public void setNumberOfreqedUnlocks(short numberOfreqedUnlocks) {
		this.numberOfreqedUnlocks = numberOfreqedUnlocks;
	}
	public short getNumberOfreqedLocks() {
		return numberOfreqedLocks;
	}
	public void setNumberOfreqedLocks(short numberOfreqedLocks) {
		this.numberOfreqedLocks = numberOfreqedLocks;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public LockingANDXRange32[] getUnlocks32() {
		return unlocks32;
	}
	public void setUnlocks32(LockingANDXRange32[] unlocks32) {
		this.unlocks32 = unlocks32;
	}
	public LockingANDXRange64[] getUnlocks64() {
		return unlocks64;
	}
	public void setUnlocks64(LockingANDXRange64[] unlocks64) {
		this.unlocks64 = unlocks64;
	}
	public LockingANDXRange32[] getLocks32() {
		return Locks32;
	}
	public void setLocks32(LockingANDXRange32[] locks32) {
		Locks32 = locks32;
	}
	public LockingANDXRange64[] getLocks64() {
		return locks64;
	}
	public void setLocks64(LockingANDXRange64[] locks64) {
		this.locks64 = locks64;
	}
	@Override
	public boolean isMalformed() {
		// TODO Auto-generated method stub
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	@Override
	public String toString(){
		return String.format("First Level : Locking AndX Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"andxCommand = 0x%s , andxReserved = 0x%s , andxOffset = 0x%s\n"+
				"fid = 0x%s , typeOfLock = 0x%s , newOpLockLevel = 0x%s\n"+
				"timeOut = 0x%s , numberOfReqedUnlocks = 0x%s , numberofreqedLocks = 0x%s\n"+
				"byteCount = 0x%s\n"+
				"Locking Andx not showed",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.andxCommand), Integer.toHexString(this.andxReserved) , Integer.toHexString(this.andxOffset),
				Integer.toHexString(this.fid) , Integer.toHexString(this.typeOfLock) , Integer.toHexString(this.newOpLockLevel),
				Integer.toHexString(this.timeout) , Integer.toHexString(this.numberOfreqedUnlocks) , Integer.toHexString(this.numberOfreqedLocks),
				Integer.toHexString(this.byteCount));
	}
}

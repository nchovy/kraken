package org.krakenapps.pcap.decoder.smb;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.pcap.decoder.smb.request.NegotiateRequest;
import org.krakenapps.pcap.decoder.smb.request.NtTransactRequest;
import org.krakenapps.pcap.decoder.smb.request.Transaction2Request;
import org.krakenapps.pcap.decoder.smb.request.TransactionRequest;
import org.krakenapps.pcap.decoder.smb.response.NegotiateResponse;
import org.krakenapps.pcap.decoder.smb.response.NegotiateSecurityExtendResponse;
import org.krakenapps.pcap.decoder.smb.rr.NtTransactCommand;
import org.krakenapps.pcap.decoder.smb.rr.SmbCommand;
import org.krakenapps.pcap.decoder.smb.rr.Transaction2Command;
import org.krakenapps.pcap.decoder.smb.rr.TransactionCommand;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbDialect;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.decoder.tcp.TcpSessionKey;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
import org.krakenapps.pcap.util.ChainBuffer;

public class SmbSession {
	private TcpSessionKey key;
	private SmbHeader useSessionHeader;
	private SmbData useSessionData;
	private SmbHeader lastHeader;
	private SmbData lastData;
	private int dialectIndex;
	private SmbDialect useDialect;
	private NegotiateResponse negotiateResponseData;
	private NegotiateSecurityExtendResponse negotiateExtResponseData;
	private SmbHeader negotiateRequestHeader;
	private SmbHeader negotiateResponseHeader;
	private NegotiateRequest negotiateRequestData;
	private boolean isExt;
	private Map<SmbCommand, SmbHeader> headerMap = new HashMap<SmbCommand, SmbHeader>();
	private Map<SmbCommand, SmbData> dataMap = new HashMap<SmbCommand, SmbData>();
	private Buffer transBuffer;
	private Buffer parameterBuffer;

	public SmbSession() {
		isExt = false;
		transBuffer = new ChainBuffer();
		parameterBuffer = new ChainBuffer();
	}

	public SmbSession(TcpSessionKey key) {
		this.key = key;
		isExt = false;
		transBuffer = new ChainBuffer();
		parameterBuffer = new ChainBuffer();
	}

	public Buffer getParameterBuffer() {
		return parameterBuffer;
	}

	public void setParameterBuffer(Buffer parameterBuffer) {
		this.parameterBuffer = parameterBuffer;
	}

	public Buffer getTransBuffer() {
		return transBuffer;
	}

	public void addTransBuffer(Buffer transBuffer) {
		this.transBuffer.addLast(transBuffer);
	}

	public void setTransBuffer(Buffer transBuffer) {
		this.transBuffer = transBuffer;
	}

	public void getSession(SmbHeader header) {
		getSessionHeader(header.getCommand());
		getSessionData(header.getCommand());
	}

	public SmbHeader getUseSessionHeader() {
		return useSessionHeader;
	}

	public SmbData getUseSessionData() {
		return useSessionData;
	}

	public SmbHeader getSessionHeader(SmbCommand command) {
		useSessionHeader = headerMap.get(command);
		if (useSessionHeader != null) {
			lastHeader = useSessionHeader;
			headerMap.remove(command);
		} else {
			useSessionHeader = lastHeader;
		}
		return useSessionHeader;
	}

	public void setSessionHeader(SmbHeader header) {
		headerMap.put(header.getCommand(), header);
	}

	public SmbData getSessionData(SmbCommand command) {
		useSessionData = dataMap.get(command);
		if (useSessionData != null) {
			lastData = useSessionData;
			dataMap.remove(command);
		} else {
			useSessionData = lastData;
		}
		return useSessionData;
	}

	public void setSessionData(SmbHeader header, SmbData data) {
		dataMap.put(header.getCommand(), data);
	}

	public Transaction2Command getSessionTrans2Command() {
		Buffer b = new ChainBuffer();
		b.addLast(((Transaction2Request) this.useSessionData).getSetup());
		if (b.readableBytes() == 0) {
			return null;
		}
		return Transaction2Command.parse(ByteOrderConverter.swap(b.getShort()));
	}

	public TransactionCommand getSessionTransCommand() {
		Buffer b = new ChainBuffer();
		b.addLast(((TransactionRequest) this.useSessionData).getSetup());
		if (b.readableBytes() == 0) {
			return null;
		}
		return TransactionCommand.parse(ByteOrderConverter.swap(b.getShort()));
	}

	public NtTransactCommand getSessionNtTransCommand() {
		Buffer b = new ChainBuffer();
		b.addLast(((NtTransactRequest) this.useSessionData).getSetup());
		b.rewind();
		if (b.readableBytes() == 0) {
			return null;
		}
		return NtTransactCommand.parse(ByteOrderConverter.swap(b.getShort()));
	}

	public boolean isExt() {
		return isExt;
	}

	public SmbDialect getUseDialect() {
		return useDialect;
	}

	public void setUseDialect(SmbDialect useDialect) {
		this.useDialect = useDialect;
	}

	public void setExt(boolean isExt) {
		this.isExt = isExt;
	}

	public SmbData getNegotiateResponseData() {
		if (isExt())
			return negotiateExtResponseData;
		else
			return negotiateResponseData;
	}

	public void setNegotiateResponseData(NegotiateResponse negotiateResponseData) {
		this.negotiateResponseData = negotiateResponseData;
	}

	public void setNegotiateExtResponseData(NegotiateSecurityExtendResponse negotiateExtResponseData) {
		this.negotiateExtResponseData = negotiateExtResponseData;
	}

	public SmbHeader getNegotiateRequestHeader() {
		return negotiateRequestHeader;
	}

	public void setNegotiateRequestHeader(SmbHeader negotiateRequestHeader) {
		this.negotiateRequestHeader = negotiateRequestHeader;
	}

	public SmbHeader getNegotiateResponseHeader() {
		return negotiateResponseHeader;
	}

	public void setNegotiateResponseHeader(SmbHeader negotiateResponseHeader) {
		this.negotiateResponseHeader = negotiateResponseHeader;
	}

	public NegotiateRequest getNegotiateRequestData() {
		return negotiateRequestData;
	}

	public void setNegotiateRequestData(NegotiateRequest negotiateRequestData) {
		this.negotiateRequestData = negotiateRequestData;
	}

	public int getDialectIndex() {
		return dialectIndex;
	}

	public void setDialectIndex(int dialectIndex) {
		this.dialectIndex = dialectIndex;
		this.useDialect = this.negotiateRequestData.getDialects()[dialectIndex];
	}

	public TcpSessionKey getKey() {
		return key;
	}

	public boolean isUserSecurity() {
		if (isExt())
			return this.negotiateExtResponseData.isUserSecurity();
		else
			return this.negotiateResponseData.isUserSecurity();
	}

	public boolean isEncryptPasswords() {
		if (isExt())
			return this.negotiateExtResponseData.isEncryptPasswords();
		else
			return this.negotiateResponseData.isEncryptPasswords();
	}

	public boolean isSecuritySignaturesEnable() {
		if (isExt())
			return this.negotiateExtResponseData.isSecuritySignaturesEnable();
		else
			return this.negotiateResponseData.isSecuritySignaturesEnable();
	}

	public boolean isSecuritySignaruesRequired() {
		if (isExt())
			return this.negotiateExtResponseData.isSecuritySignaruesRequired();
		else
			return this.negotiateResponseData.isSecuritySignaruesRequired();
	}

	public boolean isReserved() {
		if (isExt())
			return this.negotiateExtResponseData.isReserved();
		else
			return this.negotiateResponseData.isReserved();
	}

	// capability
	public boolean isCapRawMode() {
		if (isExt())
			return this.negotiateExtResponseData.isCapRawMode();
		else
			return this.negotiateResponseData.isCapRawMode();
	}

	public boolean isCapMpxMode() {
		if (isExt())
			return this.negotiateExtResponseData.isCapMpxMode();
		else
			return this.negotiateResponseData.isCapMpxMode();
	}

	public boolean isCapUnicode() {
		if (isExt())
			return this.negotiateExtResponseData.isCapUnicode();
		else
			return this.negotiateResponseData.isCapUnicode();
	}

	public boolean isCaplargeFiles() {
		if (isExt())
			return this.negotiateExtResponseData.isCaplargeFiles();
		else
			return this.negotiateResponseData.isCaplargeFiles();
	}

	public boolean isCapNtSmbs() {
		if (isExt())
			return this.negotiateExtResponseData.isCapNtSmbs();
		else
			return this.negotiateResponseData.isCapNtSmbs();
	}

	public boolean isCapRpcRemoteApis() {
		if (isExt())
			return this.negotiateExtResponseData.isCapRpcRemoteApis();
		else
			return this.negotiateResponseData.isCapRpcRemoteApis();
	}

	public boolean isCapStatus32() {
		if (isExt())
			return this.negotiateExtResponseData.isCapStatus32();
		else
			return this.negotiateResponseData.isCapStatus32();
	}

	public boolean isLevel2Oplocks() {
		if (isExt())
			return this.negotiateExtResponseData.isLevel2Oplocks();
		else
			return this.negotiateResponseData.isLevel2Oplocks();
	}

	public boolean isCapLockAndRead() {
		if (isExt())
			return this.negotiateExtResponseData.isCapLockAndRead();
		else
			return this.negotiateResponseData.isCapLockAndRead();
	}

	public boolean isCapNtFind() {
		if (isExt())
			return this.negotiateExtResponseData.isCapNtFind();
		else
			return this.negotiateResponseData.isCapNtFind();
	}

	public boolean isCapDfs() {
		if (isExt())
			return this.negotiateExtResponseData.isCapDfs();
		else
			return this.negotiateResponseData.isCapDfs();
	}

	public boolean isCapInforlevelpassThru() {
		if (isExt())
			return this.negotiateExtResponseData.isCapInforlevelpassThru();
		else
			return this.negotiateResponseData.isCapInforlevelpassThru();
	}

	public boolean isCapLargeReadx() {
		if (isExt())
			return this.negotiateExtResponseData.isCapLargeReadx();
		else
			return this.negotiateResponseData.isCapLargeReadx();
	}

	public boolean isCapLargeWritex() {
		if (isExt())
			return this.negotiateExtResponseData.isCapLargeWritex();
		else
			return this.negotiateResponseData.isCapLargeWritex();
	}

	public boolean isCapLwio() {
		if (isExt())
			return this.negotiateExtResponseData.isCapLwio();
		else
			return this.negotiateResponseData.isCapLwio();
	}

	public boolean isCapUnix() {
		if (isExt())
			return this.negotiateExtResponseData.isCapUnix();
		else
			return this.negotiateResponseData.isCapUnix();
	}

	public boolean isCapCompressedData() {
		if (isExt())
			return this.negotiateExtResponseData.isCapCompressedData();
		else
			return this.negotiateResponseData.isCapCompressedData();
	}

	public boolean isCapDynamicReauth() {
		if (isExt())
			return this.negotiateExtResponseData.isCapDynamicReauth();
		else
			return this.negotiateResponseData.isCapDynamicReauth();
	}

	public boolean isCapPersistentHandles() {
		if (isExt())
			return this.negotiateExtResponseData.isCapPersistentHandles();
		else
			return this.negotiateResponseData.isCapPersistentHandles();
	}

	boolean isCapExtendedSecurity() {
		if (isExt())
			return this.negotiateExtResponseData.isCapExtendedSecurity();
		else
			return this.negotiateResponseData.isCapExtendedSecurity();
	}

}

package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class RpcConnDiscOptionalData {

	private short reasonCode;
	private RpcrtOptionalData rpcInfo;
	
	public RpcConnDiscOptionalData(){
		rpcInfo = new RpcrtOptionalData();
	}
	public void parse(Buffer b){
		reasonCode = ByteOrderConverter.swap(b.getShort());
		rpcInfo.parse(b);
	}
	public short getReasonCode() {
		return reasonCode;
	}
	public void setReasonCode(short reasonCode) {
		this.reasonCode = reasonCode;
	}
	public RpcrtOptionalData getRpcInfo() {
		return rpcInfo;
	}
	public void setRpcInfo(RpcrtOptionalData rpcInfo) {
		this.rpcInfo = rpcInfo;
	}
}

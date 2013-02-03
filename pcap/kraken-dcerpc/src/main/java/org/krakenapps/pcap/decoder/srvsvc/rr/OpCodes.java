/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.rr;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.pcap.decoder.rpce.rr.StatusCode;

/**
 * @author tgnice@nchovy.com
 *
 */
public enum OpCodes {

	/* 1 - 7 error not implemented. unused*/
	NetrConnectionEnum(8),
	NetrFileEnum(9),
	NetfileGetinfo(10),
	NetFileClose(11),
	NetrSessionEnum(12),
	NetrSessionDel(13),
	NetrShareAdd(14),
	NetrShareEnum(15),
	NetrShareGetInfo(16),
	NetrShareSetInfo(17),
	NetrShareDel(18),
	NetrShareDelSticky(19),
	NetrShareCheck(20),
	NetrServerGetInfo(21),
	NetrServerSetInfo(22),
	NetServerDiskEnum(23),
	NetServerStatisticsGet(24),
	NetrServerTransportAdd(25),
	NetrServerTransportEnum(26),
	NetrServerTransportDel(27),
	NetrRemoteTOD(28),
	// opnum 29 never used remotely
	NetprPathType(30),
	NetprPathCanonicalize(31),
	NetprPathCompare(32),
	NetprNameValidate(33),
	NetprNameCanonicalize(34),
	NetprNameCompare(35),
	NetsShareEnumSticky(36),
	NetrShareDelstart(37),
	NetrShareDelCommit(38),
	NetrpGetFileSecurity(39),
	NetrpSetFileSecurity(40),
	NetrServerTransportAddEx(41),
	//opnum 42 not used
	NetrDfsGetVersion(43),
	NetrDfsCreateLocalPartition(44),
	NetrDfsDeleteLocalPartition(45),
	NetrDfsSetLocalVolumeState(46),
	NetrDfsCreateExitPoint(48),
	NetrDfsDeleteExitPoint(49),
	NetrDfsModifyPrefix(50),
	NetDfsFixLocalVolume(51),
	NetrDfsManagerReportSiteInfo(52),
	NetrServerTransportDelEx(53),
	NetrServerAliasAdd(54),
	NetrSErverAliasEnum(55),
	NetrServerAliasDel(56),
	NetrShareDeleEx(57);
	
	private int code;
	OpCodes(int code) {
		this.code = code;
	}
	static Map<Integer, OpCodes> codeMap = new HashMap<Integer, OpCodes>();
	static{
		for(OpCodes code : OpCodes.values()){
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode(){
		return code;
	}
	public static OpCodes parse(int code){
		return codeMap.get(code);
	}
}

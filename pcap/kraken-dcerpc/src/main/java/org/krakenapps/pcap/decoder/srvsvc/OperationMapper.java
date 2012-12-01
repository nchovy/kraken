/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc;

import org.krakenapps.pcap.decoder.srvsvc.rr.OpCodes;

/**
 * @author tgnice@nchovy.com
 *
 */
public class OperationMapper {

	public MessageInterface getPDU(OpCodes type){
		
		switch(type){
			case NetrConnectionEnum :
				return null;
			case NetrFileEnum :
				return null;
			case NetfileGetinfo :
				return null;
			case NetFileClose :
				return null;
			case NetrSessionEnum :
				return null;
			case NetrSessionDel :
				return null;
			case NetrShareAdd :
				return null;
			case NetrShareEnum :
				return null;
			case NetrShareGetInfo :
				return null;
			case NetrShareSetInfo :
				return null;
			case NetrShareDel :
				return null;
			case NetrShareDelSticky :
				return null;
			case NetrShareCheck :
				return null;
			case NetrServerGetInfo :
				return null;
			case NetrServerSetInfo :
				return null;
			case NetServerDiskEnum :
				return null;
			case NetServerStatisticsGet :
				return null;
			case NetrServerTransportAdd :
				return null;
			case NetrServerTransportEnum :
				return null;
			case NetrServerTransportDel :
				return null;
			case NetrRemoteTOD :
				return null;
			case NetprPathType :
				return null;
			case NetprPathCanonicalize :
				return null;
			case NetprPathCompare :
				return null;
			case NetprNameValidate :
				return null;
			case NetprNameCanonicalize :
				return null;
			case NetprNameCompare :
				return null;
			case NetsShareEnumSticky :
				return null;
			case NetrShareDelstart :
				return null;
			case NetrShareDelCommit :
				return null;
			case NetrpGetFileSecurity :
				return null;
			case NetrpSetFileSecurity :
				return null;
			case NetrServerTransportAddEx :
				return null;
			case NetrDfsGetVersion :
				return null;
			case NetrDfsCreateLocalPartition :
				return null;
			case NetrDfsDeleteLocalPartition :
				return null;
			case NetrDfsSetLocalVolumeState :
				return null;
			case NetrDfsCreateExitPoint :
				return null;
			case NetrDfsDeleteExitPoint :
				return null;
			case NetrDfsModifyPrefix :
				return null;
			case NetDfsFixLocalVolume :
				return null;
			case NetrDfsManagerReportSiteInfo :
				return null;
			case NetrServerTransportDelEx :
				return null;
			case NetrServerAliasAdd :
				return null;
			case NetrSErverAliasEnum :
				return null;
			case NetrServerAliasDel :
				return null;
			default :
				new IllegalAccessException(this+" : invalid Packet Type");
				return null;
		}
	}
}

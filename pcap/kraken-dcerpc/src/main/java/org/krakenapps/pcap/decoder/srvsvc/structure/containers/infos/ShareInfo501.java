/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos;

/**
 * @author tgnice@nchovy.com
 *
 */
public class ShareInfo501 {

	String shi501_netname;
	int shi501_type;
	String shi501_remark;
	int shi501_flags;
	
	public String getShi501_netname() {
		return shi501_netname;
	}
	public void setShi501_netname(String shi501_netname) {
		this.shi501_netname = shi501_netname;
	}
	public int getShi501_type() {
		return shi501_type;
	}
	public void setShi501_type(int shi501_type) {
		this.shi501_type = shi501_type;
	}
	public String getShi501_remark() {
		return shi501_remark;
	}
	public void setShi501_remark(String shi501_remark) {
		this.shi501_remark = shi501_remark;
	}
	public int getShi501_flags() {
		return shi501_flags;
	}
	public void setShi501_flags(int shi501_flags) {
		this.shi501_flags = shi501_flags;
	}
	
}

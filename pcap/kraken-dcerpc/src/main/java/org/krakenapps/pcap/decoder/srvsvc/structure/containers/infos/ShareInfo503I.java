/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos;

/**
 * @author tgnice@nchovy.com
 *
 */
public class ShareInfo503I {

	String shi503_netname;
	int shi503_type;
	String shi503_remark;
	int shi503_max_uses;
	String shi503_path;
	String shi503_passwd;
	String shi503_servername;
	int shi503_reserved;
	String shi503_security_descriptor; // this string's length depends on shi502_reserved; and described in MS-DTYP 
	public String getShi503_netname() {
		return shi503_netname;
	}
	public void setShi503_netname(String shi503_netname) {
		this.shi503_netname = shi503_netname;
	}
	public int getShi503_type() {
		return shi503_type;
	}
	public void setShi503_type(int shi503_type) {
		this.shi503_type = shi503_type;
	}
	public String getShi503_remark() {
		return shi503_remark;
	}
	public void setShi503_remark(String shi503_remark) {
		this.shi503_remark = shi503_remark;
	}
	public int getShi503_max_uses() {
		return shi503_max_uses;
	}
	public void setShi503_max_uses(int shi503_max_uses) {
		this.shi503_max_uses = shi503_max_uses;
	}
	public String getShi503_path() {
		return shi503_path;
	}
	public void setShi503_path(String shi503_path) {
		this.shi503_path = shi503_path;
	}
	public String getShi503_passwd() {
		return shi503_passwd;
	}
	public void setShi503_passwd(String shi503_passwd) {
		this.shi503_passwd = shi503_passwd;
	}
	public String getShi503_servername() {
		return shi503_servername;
	}
	public void setShi503_servername(String shi503_servername) {
		this.shi503_servername = shi503_servername;
	}
	public int getShi503_reserved() {
		return shi503_reserved;
	}
	public void setShi503_reserved(int shi503_reserved) {
		this.shi503_reserved = shi503_reserved;
	}
	public String getShi503_security_descriptor() {
		return shi503_security_descriptor;
	}
	public void setShi503_security_descriptor(String shi503_security_descriptor) {
		this.shi503_security_descriptor = shi503_security_descriptor;
	}
	
}

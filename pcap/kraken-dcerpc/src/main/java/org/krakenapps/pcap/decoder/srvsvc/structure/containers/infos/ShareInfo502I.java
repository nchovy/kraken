/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos;

/**
 * @author tgnice@nchovy.com
 *
 */
public class ShareInfo502I {

	String shi502_netname;
	int shi502_type;
	String shi502_remark;
	int shi502_permissions;
	int his502_max_uses;
	int shi502_current_uses;
	String shi502_path;
	String shi502_passwd;
	int shi502_reserved;
	String shi502_security_descriptor; // this string's length depends on shi502_reserved;
	public String getShi502_netname() {
		return shi502_netname;
	}
	public void setShi502_netname(String shi502_netname) {
		this.shi502_netname = shi502_netname;
	}
	public int getShi502_type() {
		return shi502_type;
	}
	public void setShi502_type(int shi502_type) {
		this.shi502_type = shi502_type;
	}
	public String getShi502_remark() {
		return shi502_remark;
	}
	public void setShi502_remark(String shi502_remark) {
		this.shi502_remark = shi502_remark;
	}
	public int getShi502_permissions() {
		return shi502_permissions;
	}
	public void setShi502_permissions(int shi502_permissions) {
		this.shi502_permissions = shi502_permissions;
	}
	public int getHis502_max_uses() {
		return his502_max_uses;
	}
	public void setHis502_max_uses(int his502_max_uses) {
		this.his502_max_uses = his502_max_uses;
	}
	public int getShi502_current_uses() {
		return shi502_current_uses;
	}
	public void setShi502_current_uses(int shi502_current_uses) {
		this.shi502_current_uses = shi502_current_uses;
	}
	public String getShi502_path() {
		return shi502_path;
	}
	public void setShi502_path(String shi502_path) {
		this.shi502_path = shi502_path;
	}
	public String getShi502_passwd() {
		return shi502_passwd;
	}
	public void setShi502_passwd(String shi502_passwd) {
		this.shi502_passwd = shi502_passwd;
	}
	public int getShi502_reserved() {
		return shi502_reserved;
	}
	public void setShi502_reserved(int shi502_reserved) {
		this.shi502_reserved = shi502_reserved;
	}
	public String getShi502_security_descriptor() {
		return shi502_security_descriptor;
	}
	public void setShi502_security_descriptor(String shi502_security_descriptor) {
		this.shi502_security_descriptor = shi502_security_descriptor;
	}
	
}

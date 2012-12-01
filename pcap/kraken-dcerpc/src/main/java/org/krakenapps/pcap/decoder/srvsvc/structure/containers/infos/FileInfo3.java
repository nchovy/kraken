/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos;

/**
 * @author tgnice@nchovy.com
 *
 */
public class FileInfo3 {

	int fi3_id;
	int fi3_permissions;
	int if3_num_locks;
	String fi3_path_name;
	String fi3_username;
	public int getFi3_id() {
		return fi3_id;
	}
	public void setFi3_id(int fi3_id) {
		this.fi3_id = fi3_id;
	}
	public int getFi3_permissions() {
		return fi3_permissions;
	}
	public void setFi3_permissions(int fi3_permissions) {
		this.fi3_permissions = fi3_permissions;
	}
	public int getIf3_num_locks() {
		return if3_num_locks;
	}
	public void setIf3_num_locks(int if3_num_locks) {
		this.if3_num_locks = if3_num_locks;
	}
	public String getFi3_path_name() {
		return fi3_path_name;
	}
	public void setFi3_path_name(String fi3_path_name) {
		this.fi3_path_name = fi3_path_name;
	}
	public String getFi3_username() {
		return fi3_username;
	}
	public void setFi3_username(String fi3_username) {
		this.fi3_username = fi3_username;
	}
	
}

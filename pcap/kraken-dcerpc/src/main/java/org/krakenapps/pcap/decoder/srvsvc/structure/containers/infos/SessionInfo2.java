/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos;

/**
 * @author tgnice@nchovy.com
 *
 */
public class SessionInfo2 {

	String sesi2_cname;
	String sesi2_username;
	int sesi2_num_opens;
	int sesi2_time;
	int sesi2_idle_time;
	int sesi2_user_flags;
	String sesi2_cltype_name;
	public String getSesi2_cname() {
		return sesi2_cname;
	}
	public void setSesi2_cname(String sesi2_cname) {
		this.sesi2_cname = sesi2_cname;
	}
	public String getSesi2_username() {
		return sesi2_username;
	}
	public void setSesi2_username(String sesi2_username) {
		this.sesi2_username = sesi2_username;
	}
	public int getSesi2_num_opens() {
		return sesi2_num_opens;
	}
	public void setSesi2_num_opens(int sesi2_num_opens) {
		this.sesi2_num_opens = sesi2_num_opens;
	}
	public int getSesi2_time() {
		return sesi2_time;
	}
	public void setSesi2_time(int sesi2_time) {
		this.sesi2_time = sesi2_time;
	}
	public int getSesi2_idle_time() {
		return sesi2_idle_time;
	}
	public void setSesi2_idle_time(int sesi2_idle_time) {
		this.sesi2_idle_time = sesi2_idle_time;
	}
	public int getSesi2_user_flags() {
		return sesi2_user_flags;
	}
	public void setSesi2_user_flags(int sesi2_user_flags) {
		this.sesi2_user_flags = sesi2_user_flags;
	}
	public String getSesi2_cltype_name() {
		return sesi2_cltype_name;
	}
	public void setSesi2_cltype_name(String sesi2_cltype_name) {
		this.sesi2_cltype_name = sesi2_cltype_name;
	}
}

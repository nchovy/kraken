/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos;

/**
 * @author tgnice@nchovy.com
 *
 */
public class SessionInfo1 {

	String sesi1_cname;
	String sesi1_username;
	int sesi1_num_opens;
	int sesi1_time;
	int sesi1_idle_time;
	int sesi1_user_flags;
	public String getSesi1_cname() {
		return sesi1_cname;
	}
	public void setSesi1_cname(String sesi1_cname) {
		this.sesi1_cname = sesi1_cname;
	}
	public String getSesi1_username() {
		return sesi1_username;
	}
	public void setSesi1_username(String sesi1_username) {
		this.sesi1_username = sesi1_username;
	}
	public int getSesi1_num_opens() {
		return sesi1_num_opens;
	}
	public void setSesi1_num_opens(int sesi1_num_opens) {
		this.sesi1_num_opens = sesi1_num_opens;
	}
	public int getSesi1_time() {
		return sesi1_time;
	}
	public void setSesi1_time(int sesi1_time) {
		this.sesi1_time = sesi1_time;
	}
	public int getSesi1_idle_time() {
		return sesi1_idle_time;
	}
	public void setSesi1_idle_time(int sesi1_idle_time) {
		this.sesi1_idle_time = sesi1_idle_time;
	}
	public int getSesi1_user_flags() {
		return sesi1_user_flags;
	}
	public void setSesi1_user_flags(int sesi1_user_flags) {
		this.sesi1_user_flags = sesi1_user_flags;
	}
	
}

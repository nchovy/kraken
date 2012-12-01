/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos;

/**
 * @author tgnice@nchovy.com
 *
 */
public class SessionInfo10 {
	String sesi10_cname;
	String sesi10_username;
	int sesi10_time;
	int sesi10_idle_time;
	public String getSesi10_cname() {
		return sesi10_cname;
	}
	public void setSesi10_cname(String sesi10_cname) {
		this.sesi10_cname = sesi10_cname;
	}
	public String getSesi10_username() {
		return sesi10_username;
	}
	public void setSesi10_username(String sesi10_username) {
		this.sesi10_username = sesi10_username;
	}
	public int getSesi10_time() {
		return sesi10_time;
	}
	public void setSesi10_time(int sesi10_time) {
		this.sesi10_time = sesi10_time;
	}
	public int getSesi10_idle_time() {
		return sesi10_idle_time;
	}
	public void setSesi10_idle_time(int sesi10_idle_time) {
		this.sesi10_idle_time = sesi10_idle_time;
	}
	
}

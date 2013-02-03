/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos;

/**
 * @author tgnice@nchovy.com
 *
 */
public class SessionInfo502 {

	String sesi502_cname;
	String sesi502_username;
	short sesi502_num_opens;
	short sesi502_time;
	short sesi502_idle_time;
	short sesi502_user_flags;
	String sesi502_cltype_name;
	String sesi502_transport;
	public String getSesi502_cname() {
		return sesi502_cname;
	}
	public void setSesi502_cname(String sesi502_cname) {
		this.sesi502_cname = sesi502_cname;
	}
	public String getSesi502_username() {
		return sesi502_username;
	}
	public void setSesi502_username(String sesi502_username) {
		this.sesi502_username = sesi502_username;
	}
	public short getSesi502_num_opens() {
		return sesi502_num_opens;
	}
	public void setSesi502_num_opens(short sesi502_num_opens) {
		this.sesi502_num_opens = sesi502_num_opens;
	}
	public short getSesi502_time() {
		return sesi502_time;
	}
	public void setSesi502_time(short sesi502_time) {
		this.sesi502_time = sesi502_time;
	}
	public short getSesi502_idle_time() {
		return sesi502_idle_time;
	}
	public void setSesi502_idle_time(short sesi502_idle_time) {
		this.sesi502_idle_time = sesi502_idle_time;
	}
	public short getSesi502_user_flags() {
		return sesi502_user_flags;
	}
	public void setSesi502_user_flags(short sesi502_user_flags) {
		this.sesi502_user_flags = sesi502_user_flags;
	}
	public String getSesi502_cltype_name() {
		return sesi502_cltype_name;
	}
	public void setSesi502_cltype_name(String sesi502_cltype_name) {
		this.sesi502_cltype_name = sesi502_cltype_name;
	}
	public String getSesi502_transport() {
		return sesi502_transport;
	}
	public void setSesi502_transport(String sesi502_transport) {
		this.sesi502_transport = sesi502_transport;
	}
			
}


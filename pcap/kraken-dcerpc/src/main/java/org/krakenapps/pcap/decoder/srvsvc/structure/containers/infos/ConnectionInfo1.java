/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos;

/**
 * @author tgnice@nchovy.com
 *
 */
public class ConnectionInfo1 {

	int coni1_id;
	int coni1_type;
	int coni1_num_opens;
	int conil1_num_users;
	int conil_time;
	String coni1_username;
	String coni1_netname;

	public int getConi1_id() {
		return coni1_id;
	}

	public void setConi1_id(int coni1_id) {
		this.coni1_id = coni1_id;
	}

	public int getConi1_type() {
		return coni1_type;
	}

	public void setConi1_type(int coni1_type) {
		this.coni1_type = coni1_type;
	}

	public int getConi1_num_opens() {
		return coni1_num_opens;
	}

	public void setConi1_num_opens(int coni1_num_opens) {
		this.coni1_num_opens = coni1_num_opens;
	}

	public int getConil1_num_users() {
		return conil1_num_users;
	}

	public void setConil1_num_users(int conil1_num_users) {
		this.conil1_num_users = conil1_num_users;
	}

	public int getConil_time() {
		return conil_time;
	}

	public void setConil_time(int conil_time) {
		this.conil_time = conil_time;
	}

	public String getConi1_username() {
		return coni1_username;
	}

	public void setConi1_username(String coni1_username) {
		this.coni1_username = coni1_username;
	}

	public String getConi1_netname() {
		return coni1_netname;
	}

	public void setConi1_netname(String coni1_netname) {
		this.coni1_netname = coni1_netname;
	}
	
	
}

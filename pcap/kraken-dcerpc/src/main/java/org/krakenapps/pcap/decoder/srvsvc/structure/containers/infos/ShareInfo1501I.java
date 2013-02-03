/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos;

/**
 * @author tgnice@nchovy.com
 *
 */
public class ShareInfo1501I {

	int shi1501_reserved;
	String shi1501_security_descriptor;
	
	public int getShi1501_reserved() {
		return shi1501_reserved;
	}
	public void setShi1501_reserved(int shi1501_reserved) {
		this.shi1501_reserved = shi1501_reserved;
	}
	public String getShi1501_security_descriptor() {
		return shi1501_security_descriptor;
	}
	public void setShi1501_security_descriptor(String shi1501_security_descriptor) {
		this.shi1501_security_descriptor = shi1501_security_descriptor;
	}
	
}

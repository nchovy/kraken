package org.krakenapps.pcap.decoder.ethernet;

public class IEEE_802_1Q {
    private int pcp;
    private int dei;
    private int vid;

    public IEEE_802_1Q(int pcp, int dei, int vid) {
	this.pcp = pcp;
	this.dei = dei;
	this.vid = vid;
    }

    public int getPriorityCodePoint() {
	return pcp;
    }

    public int getDropEligibleIndicator() {
	return dei;
    }

    public int getvLANIdentifier() {
	return vid;
    }
}

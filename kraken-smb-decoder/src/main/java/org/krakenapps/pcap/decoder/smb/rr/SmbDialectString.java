package org.krakenapps.pcap.decoder.smb.rr;

public class SmbDialectString {

	//see http://www.cifs.org/wiki/SMB_Dialects
	String coreProtocol1 = new String("PCLAN1.0");
	String coreProtocol2 = new String("PC NETWORK PROGRAM 1.0");
	String xenixExtensions1 = new String("xenix1.1");
	String xenixExtensions2 = new String("XENIX CORE");
	String corePlus = new String("MICROSOFT NETWORKS 1.03");
	String lanManager1_0 = new String("LANMAN1.0");
	String dosLanManager1_0 = new String("MICROSOFT NETWORKS 3.0");
	String lanManager1_2 = new String("LANMAN1.2");
	String lanManager2_0 = new String("LM1.2X002");
	String dosLanManager2_0 = new String("DOS LM1.2X002");
	String lanManager2_1 = new String("LANMAN2.1");
	String dosLanManager2_1 = new String("DOS LANMAN2.1");
	String []ntLanManager = new String[3];
	
	public SmbDialectString() {
		ntLanManager[0] = new String("NT LM 0.12");
		ntLanManager[1] = new String("NT LANMAN 1.0");
		ntLanManager[2] = new String("Samba");
	}
	
	public boolean isPcLan1_0(String str)
	{
		return coreProtocol1.equals(str);
	}
	public boolean isPcNetworkProgram1_0(String str)
	{
		return coreProtocol2.equals(str);
	}
	public boolean isXenix1_1(String str)
	{
		return xenixExtensions1.equals(str);
	}
	
	public boolean isXenixCore(String str)
	{
		return xenixExtensions2.equals(str);
	}
	public boolean isMicrosoftNetwork1_03(String str)
	{
		return corePlus.equals(str);
	}
	public boolean isLanMan1_0(String str)
	{
		return lanManager1_0.equals(str);
	}
	public boolean isMicrofostNetworks3_0(String str)
	{
		return dosLanManager1_0.equals(str);
	}
	public boolean isLanMan1_2(String str)
	{
		return lanManager1_2.equals(str);
	}
	public boolean isLanManager2_0(String str)
	{
		return lanManager2_0.equals(str);
	}
	public boolean isDosLanManager2_0(String str)
	{
		return dosLanManager2_0.equals(str);
	}
	public boolean isLanManager2_1(String str)
	{
		return lanManager2_1.equals(str);
	}
	public boolean isDosLanManager2_1(String str)
	{
		return dosLanManager2_1.equals(str);
	}
	public boolean isNtLanManager(String str)
	{
		return (ntLanManager[0].equals(str) || ntLanManager[1].equals(str) || ntLanManager[2].equals(str));
	}
}

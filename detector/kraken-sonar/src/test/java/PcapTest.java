/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.*;
import java.net.*;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.http.HttpDecoder;
import org.krakenapps.pcap.decoder.http.HttpProcessor;
import org.krakenapps.pcap.decoder.http.HttpRequest;
import org.krakenapps.pcap.decoder.http.HttpResponse;
import org.krakenapps.pcap.live.PcapDevice;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.PcapFileRunner;
import org.krakenapps.pcap.util.PcapLiveRunner;

@Ignore
public class PcapTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		URLTest("http://www.malwaredomains.com/files/domains.txt");

		//PcapTest();
	}
	
	public static void URLTest(String url) throws IOException {
		URL ocu = new URL(url);
	    URLConnection con = ocu.openConnection();
	    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		int count = 0;
		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
			++count;
			if( count == 20 )
				break;
		}
		in.close();     
	}

	public static void PcapTest() throws IOException {

		System.loadLibrary("kpcap");
		
		// Show Packet Capture Device List
		List<PcapDeviceMetadata> dList = PcapDeviceManager.getDeviceMetadataList();
//		for( int i=0 ; i<dList.size() ; i++ )
//		{
//			System.out.println(dList.get(i).getDescription());
//		}
		
		// Open Device
		PcapDevice d = PcapDeviceManager.open(dList.get(1).getName(), 5000);
		System.out.println("DEVICE : "+d.getMetadata().getDescription()+" has been loaded!");
		
		HttpDecoder httpdec = new HttpDecoder();
		httpdec.register(new HttpProcessor() {
			@Override
			public void onRequest(HttpRequest req)
			{
				// TODO Auto-generated method stub
				System.out.print(">> ");
				System.out.println(req.getURL());
				String header = "";
				header += "\tHost       : "+req.getHeader("Host")+"\n";
				header += "\tLanguage   : "+req.getHeader("Accept-Language")+"\n";
				header += "\tEncoding   : "+req.getHeader("Accept-Encoding")+"\n";
				header += "\tReferer    : "+req.getHeader("Referer")+"\n";
				header += "\tUser-Agent : "+req.getHeader("User-Agent")+"\n";
				header += "\tConnection : "+req.getHeader("Connection")+"\n";
				header += "\tAccept     : "+req.getHeader("Accept")+"\n";
				System.out.print( header );
			}

			@Override
			public void onResponse(HttpRequest req, HttpResponse resp) {
				// TODO Auto-generated method stub
				System.out.print("<< ");
				System.out.println(req.getURL());
				String header = "";
				header += "\tHost       : "+req.getHeader("Host")+"\n";
				header += "\tLanguage   : "+req.getHeader("Accept-Language")+"\n";
				header += "\tEncoding   : "+req.getHeader("Accept-Encoding")+"\n";
				header += "\tReferer    : "+req.getHeader("Referer")+"\n";
				header += "\tUser-Agent : "+req.getHeader("User-Agent")+"\n";
				header += "\tConnection : "+req.getHeader("Connection")+"\n";
				header += "\tAccept     : "+req.getHeader("Accept")+"\n";
				System.out.print( header );
			}

			@Override
			public void onMultipartData(Buffer buffer) {
				// TODO Auto-generated method stub
				;
			}
		});

		// live runner
		PcapLiveRunner runner = new PcapLiveRunner(d);
		runner.setTcpProcessor(Protocol.HTTP, httpdec);
		runner.run();

		// file runner
//		PcapFileRunner pfr = new PcapFileRunner(new File("test.pcap"));
//		pfr.setTcpProcessor(Protocol.HTTP, httpdec);
//		pfr.run();

	}

}
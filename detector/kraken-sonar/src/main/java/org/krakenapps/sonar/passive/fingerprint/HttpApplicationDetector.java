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
 package org.krakenapps.sonar.passive.fingerprint;

import java.text.ParseException;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.http.HttpProcessor;
import org.krakenapps.pcap.decoder.http.HttpRequest;
import org.krakenapps.pcap.decoder.http.HttpResponse;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.sonar.Metabase;
import org.krakenapps.sonar.PassiveScanner;
import org.krakenapps.sonar.httpheaderparser.HttpHeaderParser;

@Component(name = "sonar-http-app-detector")
@Provides
public class HttpApplicationDetector implements HttpProcessor {

	@Requires
	private PassiveScanner scanner;

	@Requires
	private Metabase metabase;

	@Validate
	public void start() {
		scanner.addTcpSniffer(Protocol.HTTP, this);
	}

	@Invalidate
	public void stop() {
		scanner.removeTcpSniffer(Protocol.HTTP, this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onRequest(HttpRequest req) {
		// Request
		if (req != null) {
			String strUserAgent = req.getHeader("User-Agent");
			// System.out.println("User-Agent(Raw text)>>\n"+strUserAgent);
			if (strUserAgent != null && strUserAgent.length() > 0) {
				try {
					HttpHeaderParser parser = new HttpHeaderParser();
					for (HttpApplicationMetaData result : (List<HttpApplicationMetaData>) parser.eval("User-Agent : "
							+ strUserAgent)) {
						if (result != null) {
							System.out.println(result.toString());

							req.getLocalAddress();

							// update to metabase
							metabase.updateApplication(
									metabase.updateVendor(result.getVendor()),
									result.getName(),
									result.getVersion(),
									metabase.updateIpEndPoint(req.getLocalAddress())
									);
						}
					}

					// Object result =
					// parser.eval("User-Agent : "+strUserAgent);
					// System.out.println("User-Agent(Parsed)>>\n"+result);
				} catch (ParseException e) {
					System.out.println("User-Agent(Parsed)>> Got ParseException!\n");
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onResponse(HttpRequest req, HttpResponse resp) {
		if (resp != null) {
			// Web server fingerprint
			String strServer = resp.getHeader("Server");
			// System.out.println("Server(Raw text)>>\n"+strServer);
			if (strServer != null && strServer.length() > 0) {
				try {
					HttpHeaderParser parser = new HttpHeaderParser();
					for (HttpApplicationMetaData result : (List<HttpApplicationMetaData>) parser.eval("Server : "
							+ strServer)) {
						if (result != null) {
							System.out.println(result.toString());

							// update to metabase
							metabase.updateApplication(
									metabase.updateVendor(result.getVendor()),
									result.getName(),
									result.getVersion(),
									metabase.updateIpEndPoint(req.getLocalAddress())
									);
						}
					}

					// Object result = parser.eval("Server : "+strServer);
					// System.out.println("Server(Parsed)>>\n"+result);
				} catch (ParseException e) {
					System.out.println("Server(Parsed)>> Got ParseException!\n");
				}
			}

			// Proxy fingerprint
			String strVia = resp.getHeader("Via");
			// System.out.println("Via(Raw text)>>\n"+strVia);
			if (strVia != null && strVia.length() > 0) {
				try {
					HttpHeaderParser parser = new HttpHeaderParser();
					for (HttpApplicationMetaData result : (List<HttpApplicationMetaData>) parser.eval("Via : "
							+ strServer)) {
						if (result != null) {
							System.out.println(result.toString());

							// update to metabase
							metabase.updateApplication(
									metabase.updateVendor(result.getVendor()),
									result.getName(),
									result.getVersion(),
									metabase.updateIpEndPoint(req.getLocalAddress())
									);
						}
					}

					// Object result = parser.eval("Via : "+strVia);
					// System.out.println("Via(Parsed)>>\n"+result);
				} catch (ParseException e) {
					System.out.println("Via(Parsed)>> Got ParseException!\n");
				}
			}

			// Web application(board) fingerprint
			String strContent = resp.getContent();
			String strURL = req.getURL().toString();

			// 1. Zeroboard
			boolean bIsZeroBoard = false;
			if (strURL.toUpperCase().contains("ZBOARD.PHP"))
				bIsZeroBoard = true;
			if (bIsZeroBoard) {
				System.out.println("Zeroboard found>>");

				String version = "Not found";

				// Search comments
				int it = 0;
				int nFindFrom = 0;
				while ((it = strContent.indexOf("<!--", nFindFrom)) > -1) {
					int itEnd = strContent.indexOf("-->", it);
					if (itEnd == -1)
						continue;

					String strComment = strContent.substring(it, itEnd);

					nFindFrom = it + 1;

					int nIdxS = strComment.indexOf("배포버젼");
					int nIdxE = -1;
					if (nIdxS > -1) {
						nIdxE = strComment.indexOf('\n', nIdxS);

						version = strComment.substring(nIdxS, nIdxE);

					}
				}

				// update to metabase
				metabase.updateApplication(
						metabase.updateVendor("Zero"),
						"Zeroboard",
						version,
						metabase.updateIpEndPoint(req.getLocalAddress())
						);
			}

			// Next
		}
	}

	@Override
	public void onMultipartData(Buffer buffer) {
	}

}

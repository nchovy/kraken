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
package org.krakenapps.sentry.linux.logger;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.linux.api.NicStat;
import org.krakenapps.log.api.AbstractLogger;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.SimpleLog;

public class NetworkUsageLogger extends AbstractLogger {

	public NetworkUsageLogger(String namespace, String name, String description, LoggerFactory loggerFactory) {
		super(name, description, loggerFactory);
	}

	@Override
	protected void runOnce() {
		try {
			Object[] stats1 = NicStat.getNicStats().toArray();
			Thread.sleep(1000);
			Object[] stats2 = NicStat.getNicStats().toArray();

			for (int i = 0; i < stats1.length; i++) {
				NicStat first = (NicStat) stats1[i];
				NicStat second = (NicStat) stats2[i];

				long rxBps = second.getRxBytes() - first.getRxBytes();
				long txBps = second.getTxBytes() - first.getTxBytes();
				long rxFps = second.getRxPackets() - first.getRxPackets();
				long txFps = second.getTxPackets() - first.getTxPackets();
				long rxErrorsDelta = second.getRxErrors() - first.getRxErrors();
				long txErrorsDelta = second.getTxErrors() - first.getTxErrors();
				long rxDropsDelta = second.getRxDrops() - first.getRxDrops();
				long txDropsDelta = second.getTxDrops() - first.getTxDrops();

				Map<String, Object> m = new HashMap<String, Object>();
				m.put("scope", "device");
				m.put("index", i);
				m.put("rx_bytes_delta", rxBps);
				m.put("tx_bytes_delta", txBps);
				m.put("rx_pkts_delta", rxFps);
				m.put("tx_pkts_delta", txFps);
				m.put("rx_errors_delta", rxErrorsDelta);
				m.put("tx_errors_delta", txErrorsDelta);
				m.put("rx_drops_delta", rxDropsDelta);
				m.put("tx_drops_delta", txDropsDelta);

				String msg = String.format("network usage: %s, RX[%d%%, %s bps, %s fps], TX[%d%%, %s bps, %s fps]",
						first.getName(), 0, rxBps, rxFps, 0, txBps, txFps);
				Log log = new SimpleLog(new Date(), getFullName(), "device", msg, m);
				write(log);
			}
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}
	}

}

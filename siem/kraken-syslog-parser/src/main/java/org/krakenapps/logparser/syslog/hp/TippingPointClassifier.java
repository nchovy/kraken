/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.logparser.syslog.hp;

import java.util.StringTokenizer;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.syslog.Syslog;
import org.krakenapps.syslogmon.SyslogClassifier;
import org.krakenapps.syslogmon.SyslogClassifierRegistry;

/**
 * 
 * @author xeraph
 * @since 1.4.0
 */
@Component(name = "tippingpoint-classifier")
public class TippingPointClassifier implements SyslogClassifier {
	@Requires
	private SyslogClassifierRegistry classifierRegistry;

	@Validate
	public void start() {
		classifierRegistry.register("tippingpoint", this);
	}

	@Invalidate
	public void stop() {
		if (classifierRegistry != null)
			classifierRegistry.unregister("tippingpoint");
	}

	@Override
	public String classify(Syslog syslog) {
		String line = syslog.getMessage();
		StringTokenizer tok = new StringTokenizer(line, "\t");
		for (int i = 0; i < 14; i++)
			tok.nextToken();

		return tok.nextToken();
	}

}

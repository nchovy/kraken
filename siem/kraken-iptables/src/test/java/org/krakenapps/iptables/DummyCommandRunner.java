/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.iptables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.krakenapps.iptables.impl.CommandRunner;

public class DummyCommandRunner implements CommandRunner {
	private File f;

	public DummyCommandRunner(File f) {
		this.f = f;
	}

	@Override
	public List<String> run(String cmdline) {
		List<String> lines = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				lines.add(line);
			}

			return lines;
		} catch (Exception e) {
			
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
				}
		}
		
		return lines;
	}

}

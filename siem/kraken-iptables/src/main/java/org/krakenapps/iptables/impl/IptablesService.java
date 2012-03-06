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
package org.krakenapps.iptables.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.iptables.Chain;
import org.krakenapps.iptables.NetworkAddress;
import org.krakenapps.iptables.Rule;
import org.krakenapps.iptables.Iptables;
import org.krakenapps.iptables.match.IcmpMatchExtension;
import org.krakenapps.iptables.match.MatchExtension;
import org.krakenapps.iptables.match.MatchOption;
import org.krakenapps.iptables.match.MatchParseResult;
import org.krakenapps.iptables.match.StateMatchExtension;
import org.krakenapps.iptables.match.TcpMatchExtension;
import org.krakenapps.iptables.match.UdpMatchExtension;
import org.krakenapps.iptables.target.RejectTargetExtension;
import org.krakenapps.iptables.target.TargetExtension;
import org.krakenapps.iptables.target.TargetOption;
import org.krakenapps.iptables.target.TargetParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "iptables")
@Provides
public class IptablesService implements Iptables {
	private final Logger logger = LoggerFactory.getLogger(IptablesService.class.getName());

	private CommandRunner runner;

	// option name and mapped extensions
	private static final Map<String, MatchExtension> matchExtensions;
	private static final Map<String, TargetExtension> targetExtensions;

	static {
		// match extensions
		matchExtensions = new HashMap<String, MatchExtension>();
		matchExtensions.put("tcp", new TcpMatchExtension());
		matchExtensions.put("udp", new UdpMatchExtension());
		matchExtensions.put("state", new StateMatchExtension());
		matchExtensions.put("icmp", new IcmpMatchExtension());

		// target extensions
		targetExtensions = new HashMap<String, TargetExtension>();
		targetExtensions.put("reject-with", new RejectTargetExtension());
	}

	public IptablesService() {
		this.runner = new CommandRunnerImpl();
	}

	public void setCommandRunner(CommandRunner runner) {
		this.runner = runner;
	}

	@Override
	public List<String> getChainNames() throws IOException {
		List<String> chains = new ArrayList<String>();
		List<String> lines = runner.run("iptables -L -n -v");

		for (String line : lines) {
			if (line.startsWith("Chain ")) {
				chains.add(line.split(" ")[1]);
			}
		}

		return chains;
	}

	@Override
	public List<Rule> getRules(String chainName) throws IOException {
		List<Rule> rules = new ArrayList<Rule>();

		List<String> lines = runner.run("iptables -L -n -v");
		boolean found = false;

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			String[] tokens = line.split(" ");

			if (tokens[0].equals("Chain")) {
				found = tokens[1].equals(chainName);
				i++;
				continue;
			}

			if (!found)
				continue;

			if (line.length() == 0)
				continue;

			Scanner scanner = new Scanner(line);

			// pkts and bytes
			scanner.next();
			scanner.next();

			// target column
			String target = scanner.next();

			// prot column
			String protocol = scanner.next();

			// opt column
			scanner.next();

			// in and out
			String in = scanner.next();
			String out = scanner.next();

			// source column
			NetworkAddress source = new NetworkAddress(scanner.next());

			// destination column
			NetworkAddress destination = new NetworkAddress(scanner.next());

			// matches
			List<MatchOption> matchOptions = parseMatchExtensions(scanner, line);

			// target extensions
			List<TargetOption> targetOptions = parseTargetExtensions(scanner, line);

			// create rule
			Rule rule = new Rule();

			if (!in.equals("*"))
				rule.setIn(in);

			if (!out.equals("*"))
				rule.setOut(out);

			if (!source.toString().equals("0.0.0.0/0"))
				rule.setSource(source);

			if (!destination.toString().equals("0.0.0.0/0"))
				rule.setDestination(destination);

			if (!protocol.equals("all"))
				rule.setProtocol(protocol);

			rule.setTarget(target);
			rule.setMatchOptions(matchOptions);
			rule.setTargetOptions(targetOptions);

			rules.add(rule);
		}

		return rules;
	}

	private List<MatchOption> parseMatchExtensions(Scanner scanner, String line) {
		List<MatchOption> matches = new ArrayList<MatchOption>();

		int next = scanner.match().end() + 1;
		int end = line.length();

		while (next < end) {
			if (line.charAt(next) != ' ')
				break;
			next++;
		}

		while (next < end) {
			int begin = next;
			int space = line.indexOf(' ', begin);
			String type = line.substring(begin, space);

			begin = space + 1;
			MatchExtension ext = matchExtensions.get(type);
			if (ext == null)
				break;

			MatchParseResult result = ext.parse(line, begin, end);
			if (result == null)
				break;

			next = result.getNext();
			matches.add(result.getOption());
		}

		return matches;
	}

	private List<TargetOption> parseTargetExtensions(Scanner scanner, String line) {
		List<TargetOption> options = new ArrayList<TargetOption>();

		int next = scanner.match().end() + 1;
		int end = line.length();

		while (next < end) {
			if (line.charAt(next) != ' ')
				break;
			next++;
		}

		while (next < end) {
			int begin = next;
			int space = line.indexOf(' ', begin);
			String type = line.substring(begin, space);

			begin = space + 1;
			TargetExtension ext = targetExtensions.get(type);
			if (ext == null)
				break;

			TargetParseResult result = ext.parse(line, begin, end);
			if (result == null)
				break;

			next = result.getNext();
			options.add(result.getOption());
		}

		return options;
	}

	@Override
	public void addRule(String chainName, int index, Rule rule) {
		String cmd = String.format("iptables -I %s %d %s", chainName, index, rule);
		logger.trace("kraken iptables: add rule [{}]", cmd);

		try {
			runner.run(cmd);
		} catch (IOException e) {
			logger.error("kraken iptables: failed to add rule.", e);
		}
	}

	@Override
	public void addRule(String chainName, Rule rule) {
		String cmd = String.format("iptables -A %s %s", chainName, rule);
		logger.trace("kraken iptables: add rule [{}]", cmd);

		try {
			runner.run(cmd);
		} catch (IOException e) {
			logger.error("kraken iptables: failed to add rule.", e);
		}
	}

	@Override
	public void removeRule(String chainName, int index) throws IOException {
		String cmd = String.format("iptables -D %s %d", chainName, index);
		runner.run(cmd);
	}

	@Override
	public void addRule(Chain chain, int index, Rule rule) {
		addRule(chain.name(), index, rule);
	}

	@Override
	public void addRule(Chain chain, Rule rule) {
		addRule(chain.name(), rule);
	}

	@Override
	public List<Rule> getRules(Chain chain) throws IOException {
		return getRules(chain.name());
	}

	@Override
	public void removeRule(Chain chain, int index) throws IOException {
		removeRule(chain.name(), index);
	}

	private static class CommandRunnerImpl implements CommandRunner {
		@Override
		public List<String> run(String cmdline) throws IOException {
			List<String> lines = new ArrayList<String>();

			Process child = Runtime.getRuntime().exec(cmdline);
			try {
				child.waitFor();
			} catch (InterruptedException e) {
			}

			InputStream is = null;
			if (child.exitValue() == 0)
				is = child.getInputStream();
			else
				is = child.getErrorStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				lines.add(line);
			}

			if (child.exitValue() != 0)
				throw new IllegalStateException(lines.get(0));

			return lines;
		}
	}
}

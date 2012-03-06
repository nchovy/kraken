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
package org.krakenapps.pcap.decoder.telnet;

public class DefaultTelnetProcessor implements TelnetProcessor {
	@Override
	public void onClientAnsiControl(AnsiMode mode, TelnetCommand command, int[] arguments) {
	}

	@Override
	public void onClientCommand(TelnetCommand command, TelnetOption option, byte[] data) {
	}

	@Override
	public void onClientData(String text) {
	}

	@Override
	public void onClientTitle(String title) {
	}

	@Override
	public void onServerAnsiControl(AnsiMode mode, TelnetCommand command, int[] arguments) {
	}

	@Override
	public void onServerCommand(TelnetCommand command, TelnetOption option, byte[] data) {
	}

	@Override
	public void onServerData(String text) {
	}

	@Override
	public void onServerTitle(String title) {
	}
}

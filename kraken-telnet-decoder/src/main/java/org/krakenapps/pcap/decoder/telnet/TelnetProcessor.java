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

public interface TelnetProcessor {
	void onClientCommand(TelnetCommand command, TelnetOption option, byte[] data);

	void onServerCommand(TelnetCommand command, TelnetOption option, byte[] data);

	void onClientAnsiControl(AnsiMode mode, TelnetCommand command, int[] arguments);

	void onServerAnsiControl(AnsiMode mode, TelnetCommand command, int[] arguments);

	void onClientData(String text);

	void onServerData(String text);

	void onClientTitle(String title);

	void onServerTitle(String title);
}

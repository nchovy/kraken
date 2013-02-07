/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.honey.sshd.impl;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.krakenapps.honey.sshd.HoneySshService;

public class HoneySshCommandFactory implements Factory<Command> {
	private HoneySshService sshd;

	public HoneySshCommandFactory(HoneySshService sshd) {
		this.sshd = sshd;
	}

	@Override
	public Command create() {
		HoneySshSessionImpl session = new HoneySshSessionImpl(sshd);
		session.addListener(new HoneySshHandler(session));
		return session;
	}
}

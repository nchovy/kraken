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
package org.krakenapps.fluxmon.impl;

import java.net.InetAddress;
import java.util.Date;

import org.krakenapps.fluxmon.FluxHost;

public class FluxHostImpl implements FluxHost {
	private InetAddress address;
	private Date createDateTime;
	private Date updateDateTime;

	public FluxHostImpl(InetAddress address) {
		this(address, new Date(), new Date());
	}

	public FluxHostImpl(InetAddress address, Date createDateTime, Date updateDateTime) {
		this.address = address;
		this.createDateTime = createDateTime;
		this.updateDateTime = updateDateTime;
	}

	@Override
	public InetAddress getAddress() {
		return address;
	}

	@Override
	public Date getCreateDateTime() {
		return createDateTime;
	}

	@Override
	public Date getUpdateDateTime() {
		return updateDateTime;
	}

}

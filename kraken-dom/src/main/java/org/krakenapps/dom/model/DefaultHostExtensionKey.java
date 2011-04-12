/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.dom.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class DefaultHostExtensionKey implements Serializable {
	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name = "type_id")
	private HostType type;

	@ManyToOne
	@JoinColumn(name = "ext_id")
	private HostExtension extension;

	public HostType getType() {
		return type;
	}

	public void setType(HostType type) {
		this.type = type;
	}

	public HostExtension getExtension() {
		return extension;
	}

	public void setExtension(HostExtension extension) {
		this.extension = extension;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((extension == null) ? 0 : extension.getId());
		result = prime * result + ((type == null) ? 0 : type.getId());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultHostExtensionKey other = (DefaultHostExtensionKey) obj;
		if (extension == null) {
			if (other.extension != null)
				return false;
		} else if (extension.getId() != other.extension.getId())
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (type.getId() != other.type.getId())
			return false;
		return true;
	}

}

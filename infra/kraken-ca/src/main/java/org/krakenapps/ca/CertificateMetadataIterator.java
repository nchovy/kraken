/*
 * Copyright 2013 Future Systems, Inc.
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
package org.krakenapps.ca;

import java.util.Iterator;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigIterator;

public class CertificateMetadataIterator implements Iterator<CertificateMetadata> {
	private ConfigIterator configIt;

	public CertificateMetadataIterator(ConfigIterator configIt) {
		this.configIt = configIt;
	}

	@Override
	public boolean hasNext() {
		return configIt.hasNext();
	}

	@Override
	public CertificateMetadata next() {
		Config c = configIt.next();
		return c.getDocument(CertificateMetadata.class);
	}

	public void close() {
		configIt.close();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

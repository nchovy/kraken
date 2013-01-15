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

public abstract class BaseCertificateAuthorityListener implements CertificateAuthorityListener {
	@Override
	public void onCreateAuthority(CertificateAuthority authority) {
	}

	@Override
	public void onImportAuthority(CertificateAuthority authority) {
	}

	@Override
	public void onRemoveAuthority(String name) {
	}

	@Override
	public void onRevokeCert(CertificateAuthority authority, CertificateMetadata cm, RevocationReason reason) {
	}

	@Override
	public void onIssueCert(CertificateAuthority authority, CertificateMetadata cm) {
	}
}

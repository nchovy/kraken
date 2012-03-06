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
package org.krakenapps.siem;

import java.util.Collection;
import org.krakenapps.logstorage.Log;
import org.krakenapps.siem.model.ManagedLogger;

public interface LogServer {
	void write(Log log);

	Collection<ManagedLogger> getManagedLoggers();

	ManagedLogger getManagedLogger(String fullName);

	void createManagedLogger(ManagedLogger logger);

	void removeManagedLogger(ManagedLogger logger);

	void addNormalizedLogListener(String category, NormalizedLogListener callback);

	void removeNormalizedLogListener(String category, NormalizedLogListener callback);

}

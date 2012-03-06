/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.filter.exception;

import org.krakenapps.filter.MessageSpec;

/**
 * Unchecked exception thrown when filters can not be bound cause of message
 * specification mismatch.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class MessageSpecMismatchException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String fromFilterId;
	private String toFilterId;
	private MessageSpec outputMessageSpec;
	private MessageSpec[] inputMessageSpecs;

	public MessageSpecMismatchException(String fromFilterId, String toFilterId,
			MessageSpec outputMessageSpec, MessageSpec[] inputMessageSpec) {
		this.fromFilterId = fromFilterId;
		this.toFilterId = toFilterId;
		this.outputMessageSpec = outputMessageSpec;
		this.inputMessageSpecs = inputMessageSpec;
	}

	@Override
	public String getMessage() {
		return String.format("message spec does not match: %s -> %s\n"
				+ "source filter message spec: %s\n" + "destination filter message specs: %s\n",
				fromFilterId, toFilterId, outputMessageSpec.getName(),
				getCommaSeparatedInputMessageSpecs());
	}

	private String getCommaSeparatedInputMessageSpecs() {
		String buffer = "";

		int length = inputMessageSpecs.length;
		for (int i = 0; i < length; i++) {
			MessageSpec spec = inputMessageSpecs[i];
			buffer += spec.getName();
			if (i < length - 1) {
				buffer += ", ";
			}
		}
		return buffer;
	}
}

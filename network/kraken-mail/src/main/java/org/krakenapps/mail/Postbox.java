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
package org.krakenapps.mail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

public class Postbox {
	private Store store;
	private Folder folder;

	public Postbox(Store store) {
		this.store = store;
	}

	public Store getStore() {
		return store;
	}

	public boolean isConnected() {
		return store.isConnected();
	}

	public Folder getFolder() {
		return folder;
	}

	public void openFolder(String folderName) throws MessagingException {
		if (folder != null && folder.getName().equals(folderName))
			return;

		closeFolder();

		if (folderName == null)
			folder = store.getDefaultFolder();
		else
			folder = store.getFolder(folderName);
		folder.open(Folder.READ_ONLY);
	}

	public void closeFolder() throws MessagingException {
		if (folder != null) {
			folder.close(false);
			folder = null;
		}
	}

	public List<Message> getMessages() throws MessagingException {
		if (folder == null)
			throw new IllegalStateException("folder not opened");

		List<Message> msgs = Arrays.asList(folder.getMessages());
		Collections.reverse(msgs);
		return msgs;
	}

	public List<Message> getMessages(int offset, int limit) throws MessagingException {
		if (folder == null)
			throw new IllegalStateException("folder not opened");

		List<Message> msgs = Arrays.asList(folder.getMessages());
		Collections.reverse(msgs);
		int size = msgs.size();
		return msgs.subList(Math.min(size, offset), Math.min(size, offset + limit));
	}

	public Message getMessage(int id) throws MessagingException {
		if (folder == null)
			throw new IllegalStateException("folder not opened");

		return folder.getMessage(id);
	}

	public void disconnect() throws MessagingException {
		closeFolder();
		store.close();
	}
}

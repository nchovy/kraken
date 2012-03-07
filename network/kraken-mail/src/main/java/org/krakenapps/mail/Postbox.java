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

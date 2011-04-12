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
package org.krakenapps.pcap.decoder.smtp.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.krakenapps.pcap.decoder.smtp.SmtpData;

/**
 * @author mindori
 */
public class SmtpDataImpl implements SmtpData {
	private MimeMessage message;

	private Set<InternetAddress> fromAddrs;
	private Set<InternetAddress> toAddrs;
	private Set<InternetAddress> ccAddrs;
	private String subject;
	private String contentType;

	private String textContent;
	private String htmlContent;

	private Set<String> attachmentNames;
	private Map<String, InputStream> attachments;

	public SmtpDataImpl(MimeMessage message) {
		this.message = message;
		fromAddrs = new HashSet<InternetAddress>();
		toAddrs = new HashSet<InternetAddress>();
		ccAddrs = new HashSet<InternetAddress>();
		attachmentNames = new HashSet<String>();
		attachments = new HashMap<String, InputStream>();

		makeSmtpData();
	}

	@Override
	public MimeMessage getMimeMessage() {
		return message;
	}

	@Override
	public int getSize() {
		try {
			return message.getSize();
		} catch (MessagingException e) {
			return 0;
		}
	}

	@Override
	public Date getSentDate() {
		try {
			return message.getSentDate();
		} catch (MessagingException e) {
			return null;
		}
	}

	@Override
	public Set<InternetAddress> getFrom() {
		return fromAddrs;
	}

	@Override
	public Set<InternetAddress> getTo() {
		return toAddrs;
	}

	@Override
	public Set<InternetAddress> getCc() {
		return ccAddrs;
	}

	@Override
	public String getSubject() {
		return subject;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String getTextContent() {
		return textContent;
	}

	@Override
	public String getHtmlContent() {
		return htmlContent;
	}

	@Override
	public Set<String> getAttachmentNames() {
		return attachmentNames;
	}

	private void makeSmtpData() {
		try {
			setFrom();
			setTo();
			setCc();
			setSubject();
			setContentType();

			extractAttachments();
		} catch (MessagingException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		}
	}

	private void setFrom() throws MessagingException, UnsupportedEncodingException {
		if (message.getFrom() == null)
			return;

		Address[] fromAddr = message.getFrom();
		for (int i = 0; i < fromAddr.length; i++) {
			fromAddrs.add(new InternetAddress(MimeUtility.decodeText(MimeUtility.unfold(fromAddr[i].toString()))));
		}
	}

	private void setTo() throws MessagingException, UnsupportedEncodingException {
		if (message.getHeader("to") != null) {
			String[] toAddr = message.getHeader("to");
			for(String s : toAddr) { 
				String[] s2 = s.split(",");
				if(s2.length > 1) {
					for(String s3 : s2) { 
						toAddrs.add(new InternetAddress(MimeUtility.decodeText(MimeUtility.unfold(s3))));
					}
				}
				else {
					toAddrs.add(new InternetAddress(MimeUtility.decodeText(MimeUtility.unfold(s))));
				}
			}
		}
	}

	private void setCc() throws MessagingException, UnsupportedEncodingException {
		if (message.getHeader("cc") != null) {
			String[] ccAddr = message.getHeader("cc");
			for(String s : ccAddr) { 
				String[] s2 = s.split(",");
				if(s2.length > 1) {
					for(String s3 : s2) { 
						ccAddrs.add(new InternetAddress(MimeUtility.decodeText(MimeUtility.unfold(s3))));
					}
				}
				else {
					ccAddrs.add(new InternetAddress(MimeUtility.decodeText(MimeUtility.unfold(s))));
				}
			}
		}
	}

	@Override
	public InputStream getAttachment(String fileName) {
		return attachments.get(fileName);
	}

	private void setSubject() throws MessagingException {
		if (message.getSubject() != null)
			subject = message.getSubject();
	}

	private void setContentType() throws MessagingException {
		if (message.getContentType() != null)
			contentType = message.getContentType();
	}

	private void extractAttachments() throws IOException, MessagingException {
		if (message.getContent() instanceof Multipart) {
			Multipart mp = (Multipart) message.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				BodyPart bp = mp.getBodyPart(i);
				getMultipart(bp, bp.getContent());
			}
		}
	}
	
	private void getMultipart(BodyPart bp, Object content) throws IOException, MessagingException {
		if (!(content instanceof Multipart)) {
			if (bp.getFileName() != null && content instanceof InputStream) {
				String fileName = MimeUtility.decodeText(MimeUtility.unfold(bp.getFileName()));
				attachmentNames.add(fileName);
				attachments.put(fileName, (InputStream) content);
			}
			else if (content instanceof String) {
				String[] contentType = bp.getContentType().split(";");
				if (contentType[0].matches("text/html")) {
					if (htmlContent != null)
						return;
					htmlContent = (String) content;
				} else {
					if (textContent != null)
						return;
					textContent = (String) content;
				}
			}
			return;
		}
		Multipart mp = (Multipart) content;
		for (int i = 0; i < mp.getCount(); i++) {
			BodyPart newBp = mp.getBodyPart(i);
			getMultipart(newBp, newBp.getContent());
		}
	}
}

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
package org.krakenapps.pcap.decoder.msn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.krakenapps.pcap.decoder.tcp.TcpProcessor;
import org.krakenapps.pcap.decoder.tcp.TcpSessionKey;
import org.krakenapps.pcap.util.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class MsnDecoder implements TcpProcessor {
	private Logger logger = LoggerFactory.getLogger(MsnDecoder.class.getName());

	private Set<MsnProcessor> callbacks;
	private Map<TcpSessionKey, MsnSession> sessionMap;

	public MsnDecoder() {
		callbacks = new HashSet<MsnProcessor>();
		sessionMap = new HashMap<TcpSessionKey, MsnSession>();
	}

	public void register(MsnProcessor processor) {
		callbacks.add(processor);
	}

	public void unregister(MsnProcessor processor) {
		callbacks.remove(processor);
	}

	@Override
	public void handleTx(TcpSessionKey sessionKey, Buffer data) {
		MsnSession session = sessionMap.get(sessionKey);

		byte[] t = new byte[data.readableBytes()];
		data.gets(t);
		
		Buffer txBuffer = session.getTxBuffer();
		txBuffer.addLast(t);
		
		getChatContent(session, txBuffer, t);
	}

	@Override
	public void handleRx(TcpSessionKey sessionKey, Buffer data) {
		MsnSession session = sessionMap.get(sessionKey);
		
		byte[] t = new byte[data.readableBytes()];
		data.gets(t);
		
		Buffer rxBuffer = session.getRxBuffer();
		rxBuffer.addLast(t);
		
		getChatContent(session, rxBuffer, t);
	}

	@Override
	public void onEstablish(TcpSessionKey session) {
		if (logger.isDebugEnabled())
			logger.debug("-> Msn Session Established: " + (int) session.getClientPort() + " -> "
					+ (int) session.getServerPort());
		sessionMap.put(session, new MsnSession());
	}

	@Override
	public void onFinish(TcpSessionKey session) {
		if (logger.isDebugEnabled())
			logger.debug("-> Msn Session Closed: \n" + "Client Port: "
					+ (int) session.getClientPort() + "\nServer Port: "
					+ (int) session.getServerPort());
		sessionMap.remove(session);
	}

	@Override
	public void onReset(TcpSessionKey session) {
		MsnSession msnSession = sessionMap.get(session);
		if (msnSession != null) {
			if (logger.isDebugEnabled())
				logger.debug("Deallocate tx, rx buffer and remove Msn session.");
			msnSession.clear();
			sessionMap.remove(session);
		}
	}

	private void getChatContent(MsnSession session, Buffer buffer, byte[] data) {
		String content = new String(data);
		String[] token = content.split("\r\n");
		
		boolean isFindCommand = false;
		int tokenIndex = 0;

		while (!isFindCommand && tokenIndex < token.length) {
			if (token[tokenIndex].matches("^USR.+")) {
				// USR: normal case. ANS: called by opponent.
				String[] usrCommand = token[tokenIndex].split(";");
				String[] userAddress = usrCommand[0].split(" ");
				session.setMsnUserAddress(userAddress[userAddress.length - 1]);
				isFindCommand = true;
			}

			else if (token[tokenIndex].matches("^ANS.+") && session.getMsnUserAddress() == null) {
				String[] usrCommand = token[0].split(";");
				String[] userAddress = usrCommand[0].split(" ");
				session.setMsnUserAddress(userAddress[2]);
				isFindCommand = true;
			}

			else if (token[tokenIndex].matches("^MSG.+")) {
				// get declared msnPayload length. split method of String is
				// incomplete.
				int k = 0;
				int spaceIndex = 0;
				
				while (data[k] != 0x0d) {
					if (data[k] == 0x20)
						spaceIndex = k;
					k++;
				}
				byte[] payloadLenBytes = new byte[(k - 1) - spaceIndex];
				for (int i = 0; i < payloadLenBytes.length; i++)
					payloadLenBytes[i] = data[(spaceIndex + 1) + i];

				int msnPayloadLen = session.getMsnPayloadLength();
				if (msnPayloadLen == -1) {
					// recording declared msnPayload length
					msnPayloadLen = Integer.parseInt(new String(payloadLenBytes));
					session.setMsnPayloadLength(msnPayloadLen);
				}

				int arrivedPayloadLen = 0;
				if (!session.isGetDeclaredPayloadLength()) {
					// token[k(k is MSG Header index)].length() + 2(\r\n) is MSG
					// Header length.
					arrivedPayloadLen = data.length - (token[0].length() + 2);
					// session.setGetDeclaredPayloadLength(true);
				} else
					arrivedPayloadLen = data.length;

				int beforeArrivedPayloadLength = session.getArrivedPayloadLength();
				if (session.getMsnPayloadLength() > beforeArrivedPayloadLength + arrivedPayloadLen) {
					// declared payload length as many as real payload length.
					// recording arrived msnPayload length.
					session.setArrivedPayloadLength(beforeArrivedPayloadLength + arrivedPayloadLen);
					return;
				} else {
					int capacity = buffer.readableBytes();
					byte[] msnData = new byte[capacity];
					buffer.gets(msnData);

					int index = 0;
					int contentLength = 0;

					while (index < msnData.length) {
						if (msnData[index] == 0x0d) {
							// real content length.
							contentLength = msnData.length - (index + 2);
							break;
						}
						index++;
					}

					String msnDataStr = new String(msnData);
					String[] lines = msnDataStr.split("\r\n");
					String[] param = lines[0].split(" ");

					String fromAddr = "";
					if (param[1].matches("^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$"))
						fromAddr = param[1];
					else
						fromAddr = session.getMsnUserAddress();

					int declaredPayloadLength = session.getMsnPayloadLength();

					if (contentLength > declaredPayloadLength && tokenIndex == 0) {
						if (session.isTruncated()) {
							int remainDataLength = session.getRemainDataLength();
							byte[] truncatedMsnData = session.getTruncatedMsgData();
							int recordIndex = session.getMsnPayloadLength() - remainDataLength;
							int offset = 0;
							while (offset < msnData.length) {
								truncatedMsnData[offset + recordIndex] = msnData[offset];
								offset++;
							}
							Session mailSession = Session.getDefaultInstance(new Properties());
							InputStream is = new ByteArrayInputStream(truncatedMsnData, 0,
									truncatedMsnData.length);
							handlingMsnContent(fromAddr, mailSession, is);

							session.initTruncatedInstance();
							getMsgMesssage(session, msnData, offset, fromAddr);
						} else {
							getMsgMesssage(session, msnData, 0, fromAddr);
						}
					} else {
						if (session.isTruncated()) {
							int remainDataLength = session.getRemainDataLength();
							byte[] truncatedMsnData = session.getTruncatedMsgData();
							int recordIndex = session.getMsnPayloadLength() - remainDataLength;
							int offset = 0;
							while (offset < msnData.length) {
								truncatedMsnData[offset + recordIndex] = msnData[offset];
								offset++;
							}

							if (remainDataLength > msnData.length) {
								int newRemainDataLength = remainDataLength - msnData.length;
								session.setRemainDataLength(newRemainDataLength);
								session.setTruncatedMsgData(truncatedMsnData);
								return;
							} else {
								Session mailSession = Session.getDefaultInstance(new Properties());
								InputStream is = new ByteArrayInputStream(truncatedMsnData, 0,
										truncatedMsnData.length);
								handlingMsnContent(fromAddr, mailSession, is);

								session.initTruncatedInstance();
								getMsgMesssage(session, msnData, offset, fromAddr);
							}
						} else {
							Session mailSession = Session.getDefaultInstance(new Properties());
							InputStream is = new ByteArrayInputStream(msnData, 0, msnData.length);
							handlingMsnContent(fromAddr, mailSession, is);
						}
					}
				}
				isFindCommand = true;
			}
			tokenIndex++;
		}
		session.clear();
	}
	
	private void getMsgMesssage(MsnSession session, byte[] msnData, int msnDataStartPoint,
			String fromAddr) {
		Session mailSession = Session.getDefaultInstance(new Properties());
		InputStream is;

		int lengthIndex = getLengthIndex(msnData, msnDataStartPoint);
		int declaredPayloadLength;
		if (session.isGetDeclaredPayloadLength()) {
			declaredPayloadLength = session.getDeclaredPayloadLength();
		} else {
			declaredPayloadLength = getDeclaredLength(msnData, lengthIndex);
			session.setDeclaredPayloadLength(declaredPayloadLength);
			session.setGetDeclaredPayloadLength(true);
		}

		while (true) {
			// read range: MSG header of MSN Data + declaredPayloadLength.
			while (msnData[lengthIndex] != 0x0d)
				lengthIndex++;

			int readLength = ((lengthIndex + 2) - msnDataStartPoint) + declaredPayloadLength;
			if (readLength > msnData.length - msnDataStartPoint) {
				// remainDataLength is remain length of truncated data.
				// so, cannot call chat callback method until next packet(valid
				// sequence) arrived.
				// and record bytes in MsnSession.
				int remainDataLength = readLength - (msnData.length - msnDataStartPoint);
				byte[] truncatedMsnData = new byte[readLength];

				int offset = 0;
				while (offset < (msnData.length - msnDataStartPoint)) {
					truncatedMsnData[offset] = msnData[msnDataStartPoint + offset];
					offset++;
				}
				session.setRemainDataLength(remainDataLength);
				session.setTruncatedMsgData(truncatedMsnData);
				return;
			}
			byte[] oneOfMsnData = new byte[readLength];

			int offset = 0;
			while (offset < readLength) {
				oneOfMsnData[offset] = msnData[msnDataStartPoint + offset];
				offset++;
			}

			is = new ByteArrayInputStream(oneOfMsnData, 0, oneOfMsnData.length);
			handlingMsnContent(fromAddr, mailSession, is);
			msnDataStartPoint = msnDataStartPoint + readLength;

			// If start point is EOB(end of buffer), then escape loop.
			if (msnDataStartPoint >= msnData.length - 1)
				break;
			else {
				// Is remain data compose the one of MSG Message?
				lengthIndex = getLengthIndex(msnData, msnDataStartPoint);
				declaredPayloadLength = session.getDeclaredPayloadLength();
				if (declaredPayloadLength > (msnData.length - msnDataStartPoint))
					return;
			}
		}
	}

	private int getLengthIndex(byte[] msnData, int startPoint) {
		int numOfSpace = 0;
		for (int i = startPoint; i < msnData.length; i++) {
			if (numOfSpace == 3) {
				return i;
			} else if (msnData[i] == 0x20)
				numOfSpace++;
		}
		return -1;
	}

	private int getDeclaredLength(byte[] msnData, int lengthIndex) {
		int declaredLength = -1;
		int tempIndex = lengthIndex;

		while (msnData[tempIndex] != 0x0d)
			tempIndex++;

		byte[] declaredLengthBytes = new byte[tempIndex - lengthIndex];
		for (int j = 0; j < declaredLengthBytes.length; j++)
			declaredLengthBytes[j] = msnData[lengthIndex + j];
		try {
			declaredLength = Integer.parseInt(new String(declaredLengthBytes));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return declaredLength;
	}

	private void handlingMsnContent(String fromAddr, Session mailSession, InputStream is) {
		try {
			MimeMessage msg = new MimeMessage(mailSession, is);
			if (msg.getHeader("X-MMS-IM-Format") != null) {
				dispatchChat(fromAddr, (String) msg.getContent());
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void dispatchChat(String fromAddr, String chatContent) {
		for (MsnProcessor processor : callbacks) {
			processor.onChat(fromAddr, chatContent);
		}
	}
}
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
package org.krakenapps.rss.impl;

import org.krakenapps.rss.RssCategory;
import org.krakenapps.rss.RssChannel;
import org.krakenapps.rss.RssEntry;
import org.krakenapps.rss.RssFeed;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.util.regex.Pattern;


public class RssParser {

	private enum FeedType {
		RSS1, RSS2, ATOM
	}

	private String datePattern1 = "\\d{4}-\\d{2}-\\d{2}";
	private String datePattern2 = "\\d{4}-\\d{2}-\\d{2} ";

	private RssFeed feed = new RssFeed();

	public RssFeed parse(String url) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = documentBuilderFactory
					.newDocumentBuilder();

			Document rssDoc = builder.parse(url);
			XPathFactory xpathFactory = XPathFactory.newInstance();
			RssChannel channel = new RssChannel();

			FeedType feedType = getFeedType(rssDoc);
			channel.setTitle(getChannelTitle(rssDoc, xpathFactory, feedType));
			feed.setChannel(channel);
			parseEntries(feed, xpathFactory, rssDoc, feedType, channel
					.getTitle());

			return feed;
		} catch (Exception e) {
			//logger.warn(e.getMessage());
			return null;
		}
	}

	private String getChannelTitle(Document rssXml, XPathFactory xpathFactory,
			FeedType feedType) {
		try {
			XPath xpath = xpathFactory.newXPath();
			XPathExpression xPathExpression = xpath
					.compile(getChannelXPath(feedType));
			NodeList titleNodeList = (NodeList) xPathExpression.evaluate(
					rssXml, XPathConstants.NODESET);

			if (titleNodeList.getLength() == 0)
				return null;

			return titleNodeList.item(0).getTextContent();
		} catch (XPathExpressionException e) {
			System.err.println(e.toString());
			return null;
		}
	}

	private String getChannelXPath(FeedType feedType) {
		switch (feedType) {
		case ATOM:
			return "//feed/title";
		default:
			return "//channel/title";
		}
	}

	private RssEntry getEntry(Node entryNode, FeedType feedType,
			String channelTitle) {
		RssEntry entry = null;
		switch (feedType) {
		case ATOM:
			entry = parseAtomEntry(entryNode);
			break;
		case RSS2:
			entry = parseRss2Entry(entryNode);
			break;
		default:
			entry = parseRss1Entry(entryNode);
			break;
		}

		if (entry != null)
			entry.setSource(channelTitle);

		return entry;
	}

	private RssEntry parseAtomEntry(Node entryNode) {
		NodeList childNodeList = entryNode.getChildNodes();
		RssEntry entry = new RssEntry();
		for (int i = 0; i < childNodeList.getLength(); ++i) {
			Node childNode = childNodeList.item(i);
			if (childNode.getNodeName() == "author") {
				entry.setAuthor(childNode.getFirstChild().getTextContent());
			} else if (childNode.getNodeName() == "title") {
				entry.setTitle(childNode.getTextContent());
			} else if (childNode.getNodeName() == "link") {
				NamedNodeMap attr = childNode.getAttributes();
				Node linkTypeNode = attr.getNamedItem("rel");
				if (linkTypeNode.getTextContent().equals("alternate")) {
					Node newChildNode = attr.getNamedItem("href");
					entry.setLink(newChildNode.getTextContent());
				}
			} else if (childNode.getNodeName() == "id") {
				entry.setGuid(childNode.getTextContent());
			} else if (childNode.getNodeName() == "published") {
				if (Pattern.matches(datePattern1, childNode.getTextContent())
						|| Pattern.matches(datePattern2, childNode
								.getTextContent()))
					feed.setIsHaveNotDate(true);
				entry.setCreatedAt(RssDateParser.parse(childNode
						.getTextContent()));
				entry.setIsHaveDateField(true);
			} else if (childNode.getNodeName() == "updated") {
				if (Pattern.matches(datePattern1, childNode.getTextContent())
						|| Pattern.matches(datePattern2, childNode
								.getTextContent()))
					feed.setIsHaveNotDate(true);
				entry.setModifiedAt(RssDateParser.parse(childNode
						.getTextContent()));
			} else if (childNode.getNodeName() == "content") {
				String textContent = childNode.getTextContent();
				String description = textContent.replaceAll(
						"<!\\[CDATA\\[|\\]\\]>", "");
				entry.setContent(description);
			}
		}
		return entry;
	}

	private RssEntry parseRss1Entry(Node entryNode) {
		NodeList childNodeList = entryNode.getChildNodes();
		RssEntry entry = new RssEntry();
		for (int i = 0; i < childNodeList.getLength(); ++i) {
			Node childNode = childNodeList.item(i);
			if (childNode.getNodeName() == "dc:creator") {
				entry.setAuthor(childNode.getTextContent());
			} else if (childNode.getNodeName() == "title") {
				entry.setTitle(childNode.getTextContent());
			} else if (childNode.getNodeName() == "link") {
				entry.setLink(childNode.getTextContent());
			} else if (childNode.getNodeName() == "dc:date") {
				if (Pattern.matches(datePattern1, childNode.getTextContent())
						|| Pattern.matches(datePattern2, childNode
								.getTextContent()))
					feed.setIsHaveNotDate(true);
				entry.setCreatedAt(RssDateParser.parse(childNode
						.getTextContent()));
				entry.setIsHaveDateField(true);
			} else if (childNode.getNodeName() == "description") {
				String textContent = childNode.getTextContent();
				String description = textContent.replaceAll(
						"<!\\[CDATA\\[|\\]\\]>", "");
				entry.setContent(description);
			}
		}
		return entry;
	}

	private RssEntry parseRss2Entry(Node entryNode) {
		NodeList childNodeList = entryNode.getChildNodes();
		RssEntry entry = new RssEntry();
		for (int i = 0; i < childNodeList.getLength(); ++i) {
			Node childNode = childNodeList.item(i);
			if (childNode.getNodeName() == "author") {
				entry.setAuthor(childNode.getTextContent());
			} else if (childNode.getNodeName() == "title") {
				String textContent = childNode.getTextContent();
				String textContent2 = textContent.replaceAll(
						"<!\\[CDATA\\[|\\]\\]>", "");
				String title = textContent2.replaceAll(
						"(<b>|</b>|<font [A-Za-z0-9=]*>|</font>)", "");
				entry.setTitle(title);
			} else if (childNode.getNodeName() == "link") {
				entry.setLink(childNode.getTextContent());
			} else if (childNode.getNodeName() == "guid") {
				entry.setGuid(childNode.getTextContent());
			} else if (childNode.getNodeName() == "pubDate") {
				if (Pattern.matches(datePattern1, childNode.getTextContent())
						|| Pattern.matches(datePattern2, childNode
								.getTextContent()))
					feed.setIsHaveNotDate(true);
				entry.setCreatedAt(RssDateParser.parse(childNode
						.getTextContent()));
				entry.setIsHaveDateField(true);
			} else if (childNode.getNodeName() == "dc:date") {
				if (Pattern.matches(datePattern1, childNode.getTextContent())
						|| Pattern.matches(datePattern2, childNode
								.getTextContent()))
					feed.setIsHaveNotDate(true);
				entry.setCreatedAt(RssDateParser.parse(childNode
						.getTextContent()));
				entry.setIsHaveDateField(true);
			} else if (childNode.getNodeName() == "description") {
				String textContent = childNode.getTextContent();
				String description = textContent.replaceAll(
						"<!\\[CDATA\\[|\\]\\]>", "");
				entry.setContent(description);
			} else if (childNode.getNodeName() == "category") {
				RssCategory category = new RssCategory();
				category.setName(childNode.getTextContent());
				entry.getCategories().add(category);
			}

		}
		return entry;
	}

	private String getEntryXPath(FeedType feedType) {
		switch (feedType) {
		case ATOM:
			return "//entry";
		default:
			return "//item";
		}
	}

	private void parseEntries(RssFeed rssFeed, XPathFactory xpathFactory,
			Document rssXml, FeedType feedType, String channelTitle) {
		try {
			XPath xpath = xpathFactory.newXPath();
			XPathExpression xPathExpression = xpath
					.compile(getEntryXPath(feedType));
			NodeList entryNodeList = (NodeList) xPathExpression.evaluate(
					rssXml, XPathConstants.NODESET);

			for (int i = 0; i < entryNodeList.getLength(); ++i) {
				rssFeed.addEntry(getEntry(entryNodeList.item(i), feedType,
						channelTitle));
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	private FeedType getFeedType(Document feed) {
		if (feed.getDocumentElement().getTagName() == "feed")
			return FeedType.ATOM;
		else if (feed.getDocumentElement().getTagName() == "rss")
			return FeedType.RSS2;
		else
			return FeedType.RSS1;
	}
}

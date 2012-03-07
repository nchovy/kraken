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
package org.krakenapps.rss;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class RssFeed {
	private RssChannel channel;
	private List<RssCategory> categories;
	private ArrayList<RssEntry> entries;
	private boolean isHaveNotDate;

	private int id;
	private String siteUrl;
	private String siteType;

	public RssFeed() {
		this.entries = new ArrayList<RssEntry>();
		this.categories = new ArrayList<RssCategory>();
	}

	public RssChannel getChannel() {
		return channel;
	}

	public void setChannel(RssChannel channel) {
		this.channel = channel;
	}

	public List<RssCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<RssCategory> categories) {
		this.categories = categories;
	}

	public Iterator<RssEntry> getEntries() {
		return entries.iterator();
	}

	public void addEntry(RssEntry entry) {
		entries.add(entry);
	}

	public boolean getIsHaveNotDate() {
		return isHaveNotDate;
	}

	public void setIsHaveNotDate(boolean isHaveNotDate) {
		this.isHaveNotDate = isHaveNotDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}

	public String getSiteType() {
		return siteType;
	}

	public void setSiteType(String siteType) {
		this.siteType = siteType;
	}
}
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

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

import org.krakenapps.rss.impl.RssDateParser;


public class RssEntry {
	private int id;
	private String title;
	private String author;
	private String source;
	private String link;
	private String guid;
	private String content;
	private Date createdAt;
	private Date modifiedAt;
	private boolean isHaveNotDate;
	private boolean isHaveDateField;
	private List<RssCategory> categories = new ArrayList<RssCategory>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		if (guid == null) {
			this.guid = link;
		} else
			this.guid = guid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		if (content == null) {
			this.content = "";
		} else
			this.content = content;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		if (createdAt == null) {
			Calendar cal = Calendar.getInstance();
			String dateString, timeString;
			dateString = String.format("%04d-%02d-%02d",
					cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal
							.get(Calendar.DAY_OF_MONTH));
			timeString = String.format("%02d:%02d:%02d", cal
					.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal
					.get(Calendar.SECOND));
			String date = dateString + "T" + timeString;
			this.createdAt = RssDateParser.parse(date);
		} else
			this.createdAt = createdAt;
	}

	public Date getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Date modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public boolean getIsHaveNotDate() {
		return isHaveNotDate;
	}

	public void setIsHaveNotDate(boolean isHaveNotDate) {
		this.isHaveNotDate = isHaveNotDate;
	}

	public boolean getIsHaveDateField() {
		return isHaveDateField;
	}

	public void setIsHaveDateField(boolean isHaveDateField) {
		this.isHaveDateField = isHaveDateField;
	}

	public List<RssCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<RssCategory> categories) {
		this.categories = categories;
	}
}
